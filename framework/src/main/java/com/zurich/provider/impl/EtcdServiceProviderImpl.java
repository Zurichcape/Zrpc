package com.zurich.provider.impl;

import com.zurich.config.RpcServiceConfig;
import com.zurich.enums.RpcErrorMessageEnum;
import com.zurich.enums.ServiceRegisterEnum;
import com.zurich.exception.RpcException;
import com.zurich.extension.ExtensionLoader;
import com.zurich.provider.ServiceProvider;
import com.zurich.registry.ServiceRegistry;
import com.zurich.remote.transport.netty.server.NettyRpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2024/3/16 20:50
 * @description
 */
@Slf4j
public class EtcdServiceProviderImpl implements ServiceProvider {
    /**
     * key: rpc service name(interface name + version + group)
     * value: service object
     */
    private final Map<String, Object> serviceMap;
    private final Set<String> registeredService;
    private final ServiceRegistry serviceRegistry;

    public EtcdServiceProviderImpl() {
        this.serviceMap = new ConcurrentHashMap<>();
        this.registeredService = ConcurrentHashMap.newKeySet();
        this.serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceRegisterEnum.ETCD.getName());
    }

    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if(registeredService.contains(rpcServiceName)){
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName,rpcServiceConfig.getService());
        log.info("Add service: {} and interface:{}",rpcServiceName,rpcServiceConfig.getService().getClass().getInterfaces());
    }

    @Override
    public Object getService(String rpcServiceName) {
        if(!serviceMap.containsKey(rpcServiceName)){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        return serviceMap.get(rpcServiceName);
    }

    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);
            serviceRegistry.registerService(rpcServiceConfig.getServiceName(),new InetSocketAddress(host, NettyRpcServer.PORT));
        }catch (UnknownHostException e){
            log.error("occur exception when getHostAddress",e);
        }
    }
}
