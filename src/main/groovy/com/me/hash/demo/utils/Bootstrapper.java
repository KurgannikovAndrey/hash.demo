package com.me.hash.demo.utils;

import com.me.hash.demo.data.PropertiesDBEntity;
import com.me.hash.demo.repo.NodesRepo;
import com.me.hash.demo.repo.PropertiesRepo;
import com.me.hash.demo.services.RoutingService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

@Component
@AllArgsConstructor
class Bootstrapper {

//    @Value("${is.local.test}")
//    private boolean isLocalTest;
//
//    @Value("${server.port}")
//    private String port;

    @Autowired
    private Environment env;

    private RoutingService service;

    private NodesRepo nodesRepo;

    private PropertiesRepo properties;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        System.out.println("init");
        var hostProp = new PropertiesDBEntity();
        hostProp.setName("myHost");
        var host = getMyHost();
        hostProp.setValue(host);
        System.out.println("host " + host);
        properties.save(hostProp);
        var hashProp = new PropertiesDBEntity();
        hashProp.setName("myHash");
        System.out.println("hash " + SHAId.of(host).getHash());
        hashProp.setValue(SHAId.of(host).getHash());
        properties.save(hashProp);
    }

    private String getMyHost() {
        if (env.getProperty("is.local.test", Boolean.class)) {
            System.out.println("run as local test");
            System.out.println("my host is : " + "http://localhost:" + env.getProperty("server.port"));
            return "http://localhost:" + env.getProperty("server.port");
        }
        {
            String ip = null;
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                ip = socket.getLocalAddress().getHostAddress();
            } catch (SocketException | UnknownHostException e) {
                e.printStackTrace();
            }
            if (ip == null) {
                throw new RuntimeException("Не удалось получить Ip");
            }
            return ip;
        }
    }
}
