package com.me.hash.demo.services.impl

import com.google.gson.Gson
import com.me.hash.demo.data.ResourceDBEntity
import com.me.hash.demo.entity.ResourceRestEntity
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
import wslite.http.HTTPClient
import wslite.rest.ContentType
import wslite.rest.RESTClient
import wslite.rest.RESTClientException

@Service
class UserInputProcessingServiceImpl implements UserInputProcessingService{
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
        def resourceHash = SHAId.of(findArgument)
        def target = routingService.findByHash(SHAId.of(findArgument))
        println "publish resource ${name} ${SHAId.toBInt(resourceHash.hash)} on ${target.host} ${SHAId.toBInt(target.hash)}"
        callPublishOnHost(target.host, resourceHash.hash)
        def entityToSave = new ResourceDBEntity()
        entityToSave.hash = resourceHash
        entityToSave.path = storePath
        entityToSave.name = name
        resourcesRepo.save(entityToSave)
    }

    @Override
    void download(String findArgument, String storePath, String fileName) {
        def resourceHash = SHAId.of(findArgument)
        def target = routingService.findByHash(SHAId.of(findArgument))
        println "find resource host ${fileName} ${SHAId.toBInt(resourceHash.hash)} from ${target.host} ${SHAId.toBInt(target.hash)}"
        def host = callGetHostFromHost(target.host, resourceHash.hash)
        println "found host : ${host.host}"
        println "download resource ${fileName} ${SHAId.toBInt(resourceHash.hash)} from ${host.host}}"
        def data = (callGetFileFromHost(host.host as String, resourceHash.hash, fileName).file as String).bytes as byte[]
        def file = new File(storePath)
        if (!file.exists()) {
            file.createNewFile()
        }
//        file.bytes << data
        file.append(data)
    }

    def callPublishOnHost (String host, String hash) {
        def client = new RESTClient(host)
        def eHost = propertiesRepo.findByName('myHost').get(0).value
        def entityToPost = [host : eHost, hash : hash.bytes.toString(), name : '']
        try {
            client.post(path: "/resource") {
                type ContentType.TEXT
                text new Gson().toJson(entityToPost)
            }
        } catch (RESTClientException e) {
            System.err.println("request ${e?.request?.url} ${e?.request?.contentAsString} response ${e?.response?.contentAsString}")
        }
    }

    def callGetFileFromHost (String host, String hash, String name) {
        def client = new RESTClient(host)
        def jsonSlurper = new JsonSlurper()
        def entityToPost = [host : host, hash : hash.bytes.toString(), name : name]
        def response = client.get(path : "/resource/retrieve") {
            type ContentType.TEXT
            text new Gson().toJson(entityToPost)
        }
        println "call get file url : ${response.request.url} request ${response.request.contentAsString} response ${response.contentAsString}"
        jsonSlurper.parse(response.data) as Map
    }

    def callGetHostFromHost (String host, String hash) {
        def client = new RESTClient(host)
        def jsonSlurper = new JsonSlurper()
        def entityToPost = [host : host, hash : hash.bytes.toString()]
        def response = client.get(path : "/resource/getHost") {
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
        sb.append("Nodes : \n { \n ${nodesRepo.findAll().collect{x -> "${x.toString()} int : ${SHAId.toBInt(x.hash)} \n"}} \n }")
        sb.append("Mapping : \n { \n ${mappingRepo.findAll().collect{x -> "${x.toString()} int : ${SHAId.toBInt(x.hash)} \n"}} \n }")
        sb.append("Resources : \n { \n ${resourcesRepo.findAll().collect{x -> "${x.toString()} int : ${SHAId.toBInt(x.hash)} \n"}} \n }")
    }
}
