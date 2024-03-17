package com.zurich.registry;

import com.zurich.config.RpcServiceConfig;
import com.zurich.demoService.hello.HelloService;
import com.zurich.demoService.hello.impl.HelloServiceImpl;
import com.zurich.discovery.ServiceDiscovery;
import com.zurich.discovery.impl.zk.ZkServiceDiscoveryImpl;
import com.zurich.registry.zk.ZkServiceRegistryImpl;
import com.zurich.remote.dto.RpcRequest;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2024/3/3 21:34
 * @description
 */
public class ZKServiceRegistryImplTest {
    @Test
    public void registerServiceTest(){
        ServiceRegistry serviceRegistry = new ZkServiceRegistryImpl();
        InetSocketAddress address = new InetSocketAddress("127.0.0.1",8080);
        HelloService helloService = new HelloServiceImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("g1")
                .version("v1.1")
                .service(helloService)
                .build();
        serviceRegistry.registerService(rpcServiceConfig.getRpcServiceName(),address);
        RpcRequest rpcRequest = RpcRequest.builder()
                .parameters(new Object[]{"this is a","test"})
                .paramTypes(new Class<?>[]{String.class,String.class})
                .interfaceName(rpcServiceConfig.getServiceName())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        ServiceDiscovery serviceDiscovery = new ZkServiceDiscoveryImpl();
        InetSocketAddress acquireInetSocketAddress = serviceDiscovery.findService(rpcRequest);
        assertEquals(address,acquireInetSocketAddress);
    }
}
