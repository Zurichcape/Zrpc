package com.zurich.loadBalance.loadbalancer;

import com.zurich.config.RpcServiceConfig;
import com.zurich.demoService.hello.HelloService;
import com.zurich.demoService.hello.impl.HelloServiceImpl;
import com.zurich.extension.ExtensionLoader;
import com.zurich.loadbalance.LoadBalance;
import com.zurich.remote.dto.RpcRequest;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2024/3/2 17:40
 * @description
 */
public class ConsistentHashLoadBalanceTest {
    @Test
    public void consistentHashLoadBalanceTest(){
        LoadBalance loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension("loadBalance");
        List<String> serviceUrlList = new ArrayList<>(Arrays.asList("127.0.0.1:9997", "127.0.0.1:9998", "127.0.0.1:9999"));
        HelloService helloService = new HelloServiceImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("g1")
                .version("1.1")
                .service(helloService)
                .build();
        RpcRequest rpcRequest = RpcRequest.builder()
                .parameters(new Object[]{"hello","hello world"})
                .paramTypes(new Class<?>[]{String.class,String.class})
                .interfaceName(rpcServiceConfig.getServiceName())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
//        System.out.println(rpcRequest.getInterfaceName());
        String userServiceAddress = loadBalance.selectServiceAddress(serviceUrlList,rpcRequest);
        assertEquals("127.0.0.1:9998",userServiceAddress);
    }
}
