package com.zurich.provider;

import com.zurich.config.RpcServiceConfig;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/21 19:58
 * @description
 * store and provide service object
 */
public interface ServiceProvider {
    /**
     * add service
     * @param rpcServiceConfig rpc service related attributes
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * get service
     * @param rpcServiceName
     * @return
     */
    Object getService(String rpcServiceName);

    /**
     * register service
     * @param rpcServiceConfig
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}
