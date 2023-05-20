package com.me.hash.demo.services.impl

import com.google.gson.Gson
import com.me.hash.demo.data.ResourceDBEntity
import com.me.hash.demo.repo.NodesRepo
import com.me.hash.demo.repo.PropertiesRepo
import com.me.hash.demo.repo.ResourceMappingRepo
import com.me.hash.demo.repo.ResourcesRepo
import com.me.hash.demo.services.RoutingService
import com.me.hash.demo.services.UserInputProcessingService
import com.me.hash.demo.utils.SHAId
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import wslite.rest.ContentType
import wslite.rest.RESTClient
import wslite.rest.RESTClientException

@Service
class UserInputProcessingServiceImpl implements UserInputProcessingService {
    @Autowired
    RoutingService routingService

    @Autowired
    PropertiesRepo propertiesRepo

    @Autowired
    ResourcesRepo resourcesRepo

    @Autowired
    NodesRepo nodesRepo

    @Autowired
    ResourceMappingRepo mappingRepo

    @Override
    void boot(String bootArgument) {
        bootArgument == 'root' ? routingService.bootAsRoot() : routingService.boot(bootArgument)
    }

    @Override
    void publish(String findArgument, String storePath, String name) {
        validatePublishRequest(findArgument, name, storePath)
        def resourceHash = SHAId.of(findArgument)
        def target = routingService.findByHash(SHAId.of(findArgument))
        println "publish resource ${name} ${SHAId.toBInt(resourceHash.hash)} on ${target.host} ${SHAId.toBInt(target.hash)}"
        try {
            callPublishOnHost(target.host, resourceHash.hash)
            def entityToSave = new ResourceDBEntity()
            entityToSave.hash = resourceHash
            entityToSave.path = storePath
            entityToSave.name = name
            resourcesRepo.save(entityToSave)
        } catch (Exception e) {
            println e
            throw new Exception('не удалось опубликовать файл')
        }

    }

    @Override
    void download(String findArgument, String storePath, String fileName) {
        validateDownloadRequest(findArgument, fileName, storePath)
        def resourceHash = SHAId.of(findArgument)
        def target = routingService.findByHash(SHAId.of(findArgument))
        println "find resource host ${fileName} ${SHAId.toBInt(resourceHash.hash)} from ${target.host} ${SHAId.toBInt(target.hash)}"
        def host = callGetHostFromHost(target.host, resourceHash.hash)
        println "found host : ${host.host}"
        println "download resource ${fileName} ${SHAId.toBInt(resourceHash.hash)} from ${host.host}}"
        try {
            def data = callGetFileFromHost(host.host as String, resourceHash.hash, fileName)
            def file = new File(storePath)
            file.createNewFile()
            file.append(data)
        } catch (RESTClientException restE) {
            println restE.message
            throw new Exception('не удалось найти или скачать файл')
        } catch (Exception ioE) {
            println ioE.message
            throw new Exception('не удалось сохранить файл')
        }
    }

    def callPublishOnHost(String host, String hash) {
        def client = new RESTClient(host)
        def eHost = propertiesRepo.findByName('myHost').get(0).value
        def entityToPost = [host: eHost, hash: hash.bytes.toString(), name: '']
        try {
            client.post(path: "/resource") {
                type ContentType.TEXT
                text new Gson().toJson(entityToPost)
            }
        } catch (RESTClientException e) {
            System.err.println("request ${e?.request?.url} ${e?.request?.contentAsString} response ${e?.response?.contentAsString}")
        }
    }

    def callGetFileFromHost(String host, String hash, String name) {
        def client = new RESTClient(host)
        def jsonSlurper = new JsonSlurper()
        def entityToPost = [host: host, hash: hash.bytes.toString(), name: name]
        def response = client.get(path: "/resource/retrieve") {
            type ContentType.TEXT
            text new Gson().toJson(entityToPost)
        }
        println "call get file url : ${response.request.url} request ${response.request.contentAsString} data ${response.data.hashCode()}"
        response.data
    }

    def callGetHostFromHost(String host, String hash) {
        def client = new RESTClient(host)
        def jsonSlurper = new JsonSlurper()
        def entityToPost = [host: host, hash: hash.bytes.toString()]
        def response = client.get(path: "/resource/getHost") {
            type ContentType.TEXT
            text new Gson().toJson(entityToPost)
        }
        println "call get host url : ${response.request.url} request ${response.request.contentAsString} response ${response.contentAsString}"
        jsonSlurper.parse(response.data) as Map
    }

    @Override
    String debug(String query) {
        StringBuilder sb = new StringBuilder()
        sb.append("Props : \n { \n ${propertiesRepo.findAll()} \n }")
        sb.append("Nodes : \n { \n ${nodesRepo.findAll().collect { x -> "${x.toString()} int : ${SHAId.toBInt(x.hash)} \n" }} \n }")
        sb.append("Mapping : \n { \n ${mappingRepo.findAll().collect { x -> "${x.toString()} int : ${SHAId.toBInt(x.hash)} \n" }} \n }")
        sb.append("Resources : \n { \n ${resourcesRepo.findAll().collect { x -> "${x.toString()} int : ${SHAId.toBInt(x.hash)} \n" }} \n }")
    }

    private void validateRequest(def rule, def name, def path) {
        if (!rule || !name || !path) {
            throw new Exception("Не все поля заполнены")
        }
        def props = propertiesRepo.findByName('isBooted')
        if (!(props?.size() && props?.get(0)?.value)) {
            throw new Exception("Приложение не подключено к сети узлов")
        }
    }

    private void validateDownloadRequest(def rule, def name, def storePath) {
        validateRequest(rule, name, storePath)
        def file = new File(storePath)
        if (file.exists()) {
            throw new Exception("Файл по этому пути уже существует, удалите или переместите его")
        }
    }

    private void validatePublishRequest(def rule, def name, def storePath) {
        validateRequest(rule, name, storePath)
        def file = new File(storePath)
        if (!file.exists()) {
            throw new Exception("Такого файла не существует")
        }
    }
}
