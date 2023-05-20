package com.me.hash.demo.services.impl

import com.me.hash.demo.data.ResourceDBEntity
import com.me.hash.demo.data.ResourceMappingDBEntity
import com.me.hash.demo.entity.ResourceRestEntity
import com.me.hash.demo.repo.PropertiesRepo
import com.me.hash.demo.repo.ResourceMappingRepo
import com.me.hash.demo.repo.ResourcesRepo
import com.me.hash.demo.services.ResourceService
import com.me.hash.demo.utils.SHAId
import com.me.hash.demo.utils.SHAIdComparator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

import java.nio.file.Paths
import java.util.stream.Collectors

@Service
class ResourceServiceHSQLImpl implements ResourceService {
    @Autowired
    ResourcesRepo resourcesRepo

    @Autowired
    ResourceMappingRepo resourceMappingRepo

    @Autowired
    PropertiesRepo propertiesRepo

    @Override
    byte[] getResource(ResourceRestEntity resource) {
        def path = resourcesRepo.findByName(resource.name)?.get(0)?.path
        path ? Paths.get(path)?.toFile()?.bytes : null
    }

    @Override
    void trackResource(ResourceRestEntity resource) {
        def dbEntity = new ResourceMappingDBEntity()
        dbEntity.host = resource.host
        dbEntity.hash = resource.hash
        resourceMappingRepo.save(dbEntity)
    }

    @Override
    String getHostByResourceHash(ResourceRestEntity resource) {
        resourceMappingRepo.findByHash(resource.hash).get(0).host
    }

    @Override
    List<ResourceRestEntity> getResourceMappingTable() {
        resourceMappingRepo.findAll().stream()
                .map(x -> {
                    def restEntity = new ResourceRestEntity()
                    restEntity.hash = x.hash
                    restEntity.host = x.host
                    restEntity
                }).collect(Collectors.toList())
    }

    @Override
    void fillResourcesMappingFromSource(List<ResourceRestEntity> src) {
        def myHash = SHAId.from(propertiesRepo.findByName('myHash').get(0).value)
        def comparator = new SHAIdComparator()
        def toSave = src.stream()
                .filter(x -> comparator.compare(myHash, SHAId.from(x.hash)) >= 0)
                .map(x -> {
                    def dbEntity = new ResourceMappingDBEntity()
                    dbEntity.host = x.host
                    dbEntity.hash = x.hash
                    dbEntity
                }).collect(Collectors.toList())
        resourceMappingRepo.saveAll(toSave)
    }
}
