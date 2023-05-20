package com.me.hash.demo.controllers.rest

import com.google.gson.Gson
import com.me.hash.demo.entity.ResourceRestEntity
import com.me.hash.demo.services.ResourceService
import com.me.hash.demo.utils.SHAId
import net.sf.json.groovy.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/resource")
class ResourceController {
    @Autowired
    ResourceService service

    @PostMapping
    public String postResource(@RequestBody String resource) {
        println "processing post resource, resource : ${resource}"
        def resourceJson = new JsonSlurper().parseText(resource)
        service.trackResource(new ResourceRestEntity(SHAId.fromTransportBytesRepresentation(resourceJson.hash).hash,
                resourceJson.host, resourceJson.name))
        new Gson().toJson([status : 'ok'])
    }

    @PostMapping("/retrieve")
    public String getResource(@RequestBody String resource) {//JSON status + bytes response
        println "processing get resource, resource : ${resource}"
        def resourceJson = new JsonSlurper().parseText(resource)
        def file = service.getResource(new ResourceRestEntity(SHAId.fromTransportBytesRepresentation(resourceJson.hash).hash, resourceJson.host, resourceJson.name))
        def status = file ? 'found' : 'not found'
        new Gson().toJson([status: status, file: new String(file)])
    }

    @PostMapping("/getHost")
    public String getHostByResourceHash(@RequestBody String request) {//return host with resources
        println "processing get host, param : ${request}"
        def requestJson = new JsonSlurper().parseText(request)
        def host = service.getHostByResourceHash(new ResourceRestEntity(SHAId.fromTransportBytesRepresentation(requestJson.hash).hash, '', ''))
        new Gson().toJson([status : host ? 'ok' : 'nf', host : host])
    }

    @GetMapping("/getAll")
    public String getResourceMappingTable() {
        new Gson().toJson([mappingList : service.getResourceMappingTable()])
    }
}
