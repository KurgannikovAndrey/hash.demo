package com.me.hash.demo.controllers.rest

import com.google.gson.Gson
import com.me.hash.demo.entity.HostRestEntity
import com.me.hash.demo.repo.PropertiesRepo
import com.me.hash.demo.services.RoutingService
import com.me.hash.demo.utils.SHAId
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/routing")
class RoutingController {
    @Autowired
    RoutingService service

    @PostMapping("/get")
    public String findByHash(@RequestParam String hash) {
        //println "findByHash hash : ${hash}"
        new Gson().toJson([host: service.findByHash(SHAId.fromTransportBytesRepresentation(hash)).host])
    }

    @GetMapping("/getPrev")
    public String getPrev() {
        //println "get prev"
        def prev = service.prev
        new Gson().toJson([hash: prev.hash, host: prev.host])
    }

    @PostMapping("/register/{nextOrPrev}")
    public String registerNewNode(@PathVariable String nextOrPrev, @RequestBody String host) {
        //println "register node ${host}"
        def jsonHost = new JsonSlurper().parseText(host)
        def entity = new HostRestEntity(SHAId.fromTransportBytesRepresentation(jsonHost.hash).hash as String, jsonHost.host as String)
        nextOrPrev == 'next' ? service.setNext(entity) : service.setPrev(entity)
        new Gson().toJson([status : 'ok'])
    }

    @PostMapping("/refill")
    public String refillNodes () {
        //println "refill"
        service.refill()
        new Gson().toJson([status : 'ok'])
    }

    @GetMapping("/ping")
    public String ping () {
        new Gson().toJson([status : 'ok'])
    }
}
