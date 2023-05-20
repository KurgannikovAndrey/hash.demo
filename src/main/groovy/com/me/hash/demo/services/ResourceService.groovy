package com.me.hash.demo.services

import com.me.hash.demo.entity.HostRestEntity
import com.me.hash.demo.entity.ResourceRestEntity

interface ResourceService {
    public byte[] getResource(ResourceRestEntity resource)

    public void trackResource(ResourceRestEntity resource)

    public String getHostByResourceHash(ResourceRestEntity resource)

    public List<ResourceRestEntity> getResourceMappingTable()

    public void fillResourcesMappingFromSource(List<ResourceRestEntity> src)
}