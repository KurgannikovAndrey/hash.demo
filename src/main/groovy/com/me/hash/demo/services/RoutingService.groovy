package com.me.hash.demo.services

import com.me.hash.demo.entity.HostRestEntity
import com.me.hash.demo.utils.SHAId

interface RoutingService {
    public HostRestEntity findByHash(SHAId id)
    public void refill()
    public void setNext(HostRestEntity next)
    public void setPrev(HostRestEntity prev)
    public HostRestEntity getPrev()
    public void boot(String rootHost)
    public void bootAsRoot()
}
