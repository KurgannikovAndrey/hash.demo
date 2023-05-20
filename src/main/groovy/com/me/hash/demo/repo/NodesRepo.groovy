package com.me.hash.demo.repo

import com.me.hash.demo.data.NodeDBEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface NodesRepo extends CrudRepository<NodeDBEntity, Long> {
    List<NodeDBEntity> findBySpecial(String special)
    List<NodeDBEntity> findByHash(String hash)
    @Transactional
    long deleteBySpecialIsNull()
}
