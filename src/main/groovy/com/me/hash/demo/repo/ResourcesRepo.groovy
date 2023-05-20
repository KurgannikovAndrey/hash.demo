package com.me.hash.demo.repo

import com.me.hash.demo.data.ResourceDBEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ResourcesRepo extends JpaRepository<ResourceDBEntity, Long> {
    List<ResourceDBEntity> findByName(String name)
}