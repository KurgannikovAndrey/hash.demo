package com.me.hash.demo.repo

import com.me.hash.demo.data.ResourceMappingDBEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository

import javax.annotation.Resource

@Resource
interface ResourceMappingRepo extends JpaRepository<ResourceMappingDBEntity, Long> {
    List<ResourceMappingDBEntity> findByHash(String hash)
}