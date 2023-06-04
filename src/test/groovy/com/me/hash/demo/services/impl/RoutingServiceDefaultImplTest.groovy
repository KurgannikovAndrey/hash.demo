package com.me.hash.demo.services.impl

import com.me.hash.demo.data.NodeDBEntity
import com.me.hash.demo.entity.HostRestEntity
import com.me.hash.demo.repo.NodesRepo
import com.me.hash.demo.repo.PropertiesRepo
import com.me.hash.demo.services.ResourceService
import com.me.hash.demo.utils.SHAId
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Spy
import org.testng.Assert
import wslite.rest.RESTClientException

import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.*

class RoutingServiceDefaultImplTest {

    @Mock
    private NodesRepo nodesRepo = mock(NodesRepo.class)

    @Mock
    private PropertiesRepo propertiesRepo = mock(PropertiesRepo.class)

    @Mock
    private ResourceService resourceService = mock(ResourceService.class)

    @Spy
    private RoutingServiceDefaultImpl service = spy(new RoutingServiceDefaultImpl(nodesRepo, propertiesRepo, resourceService))

    @Test
    void findByHashExceptionExpectedTest() {
        //given
        def id = SHAId.from('test')
        def exception = null
        //when
        try {
            service.findByHash(id)
        } catch (e) {
            exception = e
        }
        //then
        Assert.assertEquals(exception.class, NullPointerException.class)
    }

    @Test
    void findByHashSingleNodeTest() {
        //given
        def id = SHAId.from('test')
        def myHost = 'myHost'
        def myHash = 'myHash'
        def nodes = ([new NodeDBEntity(1, myHost, myHash, '')] * 160)
        def findNode = [hash: myHash, host: myHost]
        //when
        doReturn(nodes).when(nodesRepo).findAll()
        doReturn(findNode).when(service).callFindByHashOnHost(myHost, myHash)
        doReturn([[name: 'myHash', value: myHash]]).when(propertiesRepo).findByName('myHash')
        doReturn([[name: 'myHost', value: myHost]]).when(propertiesRepo).findByName('myHost')
        //then
        Assert.assertEquals(service.findByHash(id), new HostRestEntity(myHash, myHost))
    }

    @Test
    void findByHashManyNodesTest() {
        //given
        def targetFile = SHAId.from(new String(BigInteger.valueOf(6).toByteArray()))
        def currentNode = SHAId.from(new String(BigInteger.valueOf(5).toByteArray()))
        def sevenNode = SHAId.from(new String(BigInteger.valueOf(7).toByteArray()))
        def fourteenNode = SHAId.from(new String(BigInteger.valueOf(14).toByteArray()))
        def currentNodeHost = 'currentNodeHost'
        def sevenNodeHost = 'sevenNodeHost'
        def fourteenNodeHost = 'fourteenNodeHost'
        def targetNode = [hash: sevenNode.hash, host: sevenNodeHost]
        def nodes =
                [
                        new NodeDBEntity(1, currentNodeHost, currentNode.hash, 'current'),
                        new NodeDBEntity(2, sevenNodeHost, sevenNode.hash, 'next'),
                        new NodeDBEntity(3, fourteenNodeHost, fourteenNode.hash, 'prev'),
                ] +
                        ([new NodeDBEntity(4, sevenNodeHost, sevenNode.hash, '')] * 2) +
                        ([new NodeDBEntity(4, fourteenNodeHost, fourteenNode.hash, '')] * 2) +
                        ([new NodeDBEntity(4, currentNodeHost, currentNode.hash, '')] * 156)
        //when
        doReturn(nodes).when(nodesRepo).findAll()
        doReturn([nodes[0]]).when(nodesRepo).findByHash(currentNode.hash)
        doReturn([nodes[1]]).when(nodesRepo).findByHash(sevenNode.hash)
        doReturn([nodes[2]]).when(nodesRepo).findByHash(fourteenNode.hash)
        doReturn(targetNode).when(service).callFindByHashOnHost(sevenNodeHost, targetFile.hash)
        doReturn([[name: 'myHash', value: currentNode.hash]]).when(propertiesRepo).findByName('myHash')
        doReturn([[name: 'myHost', value: currentNodeHost]]).when(propertiesRepo).findByName('myHost')
        def result = service.findByHash(targetFile)
        println(result.hash.bytes)
        println(targetNode.hash.bytes)
        //then
        Assert.assertEquals(service.findByHash(targetFile), new HostRestEntity(targetNode.hash, targetNode.host))
    }

    @Test
    void validateHostExistTest(){
        //given
        def toValidate = new NodeDBEntity(1, 'host', 'hash', '')
        //when
        doReturn(toValidate).when(service).callPingOnHost(toValidate)
        def result = service.validateHost(toValidate)
        //then
        Assert.assertEquals(result, toValidate)
    }

    @Test
    void validateHostNoExistTest(){
        //given
        def toValidate = new NodeDBEntity(1, 'host', 'hash', '')
        def newNode = new NodeDBEntity(0, 'newHost', 'newHash', '')
        def newNodeRest = new HostRestEntity(newNode.hash, newNode.host)
        //when
        doThrow(new RESTClientException('e', null, null)).when(service).callPingOnHost(toValidate)
        doReturn(newNodeRest).when(service).findByHash(SHAId.from(toValidate.hash))
        def result = service.validateHost(toValidate)
        //then
        Assert.assertEquals(result, newNode)
    }

    @Test
    void refillInitTest(){
        //given
        def myHost = 'myHost'
        def myHash = 'myHash'
        def myRestEntity = new HostRestEntity(myHash, myHost)
        def current = new NodeDBEntity(0, 'myHost', 'myHash', 'current')
        def next = new NodeDBEntity(1, 'myHost', 'myHash', 'next')
        def prev = new NodeDBEntity(2, 'myHost', 'myHash', 'prev')
        def target = [current, next, prev] +
                (0..160).collect(i -> new NodeDBEntity(0, myHost, myHash, null))
        //when
        doReturn([[name: 'myHash', value: myHash]]).when(propertiesRepo).findByName('myHash')
        doReturn([[name: 'myHost', value: myHost]]).when(propertiesRepo).findByName('myHost')
        doReturn([current]).when(nodesRepo).findBySpecial('current')
        doReturn([next]).when(nodesRepo).findBySpecial('next')
        doReturn([prev]).when(nodesRepo).findBySpecial('prev')
        doReturn(new HostRestEntity(null, null)).when(service).validateHost(any())
        doReturn(myRestEntity).when(service).findByHash(any())
        def result = service.refill()
        //then
        Assert.assertEquals(result, target)
    }
}