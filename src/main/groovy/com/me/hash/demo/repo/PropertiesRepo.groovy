package com.me.hash.demo.repo

import com.me.hash.demo.data.PropertiesDBEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PropertiesRepo extends JpaRepository<PropertiesDBEntity, Integer>{
    List<PropertiesDBEntity> findByName(String name)
}
