package com.me.hash.demo.services.impl

import com.google.gson.Gson
import com.me.hash.demo.data.NodeDBEntity
import com.me.hash.demo.data.PropertiesDBEntity
import com.me.hash.demo.entity.HostRestEntity
import com.me.hash.demo.repo.NodesRepo
import com.me.hash.demo.repo.PropertiesRepo
import com.me.hash.demo.services.ResourceService
import com.me.hash.demo.services.RoutingService
import com.me.hash.demo.utils.SHAId
import com.me.hash.demo.utils.SHAIdComparator
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import wslite.rest.ContentType
import wslite.rest.RESTClient
import wslite.rest.RESTClientException
import wslite.rest.Response

@Service
class RoutingServiceDefaultImpl implements RoutingService {
    @Autowired
    NodesRepo nodesRepo

    @Autowired
    PropertiesRepo propertiesRepo

    @Autowired
    ResourceService resourceService

    @Override
    public HostRestEntity findByHash(SHAId id) {
        def comparator = new SHAIdComparator()
        def pool = nodesRepo.findAll().toList()
        def find = pool
                .collect { x -> SHAId.from(x.hash) }
                .findAll { x -> comparator.compare(x, id) >= 0 }
                .min { new BigInteger(it.hash.bytes).abs() }
        def result = find ? find : pool.collect { x -> SHAId.from(x.hash) }.min { new BigInteger(it.hash.bytes).abs() } as SHAId
        def host = result.hash == propertiesRepo.findByName('myHash').get(0).value ?
                propertiesRepo.findByName('myHost').get(0).value :
                callFindByHashOnHost(nodesRepo.findByHash(result.hash).get(0).host, id.hash).host as String
        new HostRestEntity(result.hash, host)
    }

    @Override
    void refill() {
        println "refill"
        def myHash = propertiesRepo.findByName('myHash').get(0).value
        def prev = nodesRepo.findBySpecial('prev').get(0)
        validateHost(prev)
        def next = nodesRepo.findBySpecial('prev').get(0)
        validateHost(next)
        nodesRepo.deleteBySpecialIsNull()
        (0..160).each {
            def targetHash = SHAId.sum(myHash, it)
            def target = findByHash(targetHash)
            def toSave = new NodeDBEntity()
            toSave.hash = target.hash
            toSave.host = target.host
            nodesRepo.save(toSave)
        }
    }

    @Override
    void setNext(HostRestEntity next) {
        updateNodeBySpecial(next, 'next')
    }

    @Override
    void setPrev(HostRestEntity prev) {
        updateNodeBySpecial(prev, 'prev')
    }

    @Override
    HostRestEntity getPrev() {
        def prevEntity = nodesRepo.findBySpecial('prev').get(0)
        new HostRestEntity(prevEntity.hash, prevEntity.host)
    }

    @Override
    void boot(String rootHost) {
        def current = new NodeDBEntity()
        current.host = propertiesRepo.findByName('myHost').get(0).value
        current.hash = SHAId.of(current.host).hash
        current.special = 'current'
        def next = callFindByHashOnHost(rootHost, propertiesRepo.findByName('myHash').get(0).value)
        def nextEntity = new NodeDBEntity()
        nextEntity.host = next.host
        nextEntity.hash = SHAId.of(next.host).hash
        nextEntity.special = 'next'
        def prev = callGetPrevOnHost(next.host as String)
        def prevEntity = new NodeDBEntity()
        prevEntity.host = prev.host
        prevEntity.hash = prev.hash
        prevEntity.special = 'prev'
        nodesRepo.saveAll([current, nextEntity, prevEntity])
        resourceService.fillResourcesMappingFromSource(callGetAllResourceMapping(next.host).mappingList as List)
        callUpdateSpecialOnHost(next.host as String, 'prev')
        refill()
        callUpdateSpecialOnHost(prev.host as String, 'next')
        callRefillOnHost(rootHost)
        setBootedTrue()
    }

    @Override
    void bootAsRoot() {
        nodesRepo.deleteAll()
        def current = new NodeDBEntity()
        current.host = propertiesRepo.findByName('myHost').get(0).value
        current.hash = SHAId.of(current.host).hash
        def next = NodeDBEntity.copy(current)
        def prev = NodeDBEntity.copy(current)
        current.special = 'current'
        next.special = 'next'
        prev.special = 'prev'
        nodesRepo.saveAll([current, next, prev])
        refill()
        setBootedTrue()
    }

    void updateNodeBySpecial(HostRestEntity node, String special) {
        def stored = nodesRepo.findBySpecial(special).get(0)
        stored = stored ? stored : new NodeDBEntity()
        stored.host = node.host
        stored.hash = node.hash
        stored.special = special
        nodesRepo.save(stored)
    }

    def callFindByHashOnHost(String host, String hash) {
        def client = new RESTClient(host)
        def jsonSlurper = new JsonSlurper()
        def response
        try {
            response = client.post(path: "/routing/get", query : [hash : hash.bytes.toString()])//hash.bytes.toString()
        } catch (RESTClientException e) {
            System.err.println("request ${e?.request?.url} ${e?.request?.contentAsString} response ${e?.response?.contentAsString}")
        }
        jsonSlurper.parse(response.data) as Map
    }

    def callGetPrevOnHost(String host) {
        //println(host)
        def client = new RESTClient(host)
        def jsonSlurper = new JsonSlurper()
        def response
        try {
            response = client.get(path: "/routing/getPrev")
        } catch (RESTClientException e) {
            System.err.println(" code ${e.response.statusCode} request ${e?.request?.url} ${e?.request?.contentAsString} response ${e?.response?.contentAsString}")
        }
        println "call /routing/getPrev"
        jsonSlurper.parse(response.data) as Map
    }

    def callGetAllResourceMapping(String host) {
        def client = new RESTClient(host)
        def jsonSlurper = new JsonSlurper()
        def response
        try {
            response = client.get(path: "/resource/getAll")
        } catch (RESTClientException e) {
            System.err.println("request ${e?.request?.url} ${e?.request?.contentAsString} response ${e?.response?.contentAsString}")
        }
        println "call /resource/getAll"
        jsonSlurper.parse(response.data) as Map
    }

    def callUpdateSpecialOnHost(String host, String special) {
        def client = new RESTClient(host)
        def jsonSlurper = new JsonSlurper()
        def eHash = propertiesRepo.findByName('myHash').get(0).value.bytes.toString()
        def eHost = propertiesRepo.findByName('myHost').get(0).value
        def entityToPost = new HostRestEntity(eHash, eHost)
        def response
        try {
            response = client.post(path: "/routing/register/${special}") {
                type ContentType.TEXT
                text new Gson().toJson(entityToPost)
            }
        } catch (RESTClientException e) {
            System.err.println("request ${e?.request?.url} ${e?.request?.contentAsString} response code ${e.response.statusCode}  content ${e?.response?.contentAsString}")
        }
        println "call /routing/register/${special} json : ${new Gson().toJson(entityToPost)} response code ${response.statusCode} content ${(response as Response).contentAsString}"
    }

    def callRefillOnHost(String host) {
        def client = new RESTClient(host)
        def jsonSlurper = new JsonSlurper()
        def response
        try {
            response = client.post(path: "/routing/refill")
        } catch (RESTClientException e) {
            System.err.println("request ${e?.request?.url} ${e?.request?.contentAsString} response ${e?.response?.contentAsString}")
        }
        println "call /routing/refill"
        jsonSlurper.parse(response.data) as Map
    }

    @Scheduled(cron = '*/30 * * * * *')
    def refillJob(){
        def props = propertiesRepo.findByName('isBooted')
        if(props?.size() && props?.get(0)?.value) {
            refill()
        }
    }

    def setBootedTrue() {
        def isBooted = new PropertiesDBEntity();
        isBooted.setName("isBooted")
        isBooted.setValue("true")
        propertiesRepo.save(isBooted)
    }

    def validateHost(NodeDBEntity entity){
        def client = new RESTClient(entity.host)
        try {
            client.get(path : "/routing/ping")
        } catch (RESTClientException e) {
            println "error during ping ${entity} : ${e.response.statusCode} ${e.response.statusMessage}"
            nodesRepo.delete(entity)
            def find = findByHash(SHAId.from(entity.hash))
            println "find host ${find}"
            def toSave = new NodeDBEntity()
            toSave.host = find.host
            toSave.hash = find.hash
            toSave.special = entity.special
            nodesRepo.save(toSave)
        }
    }
}
