package com.zurich.registry;

import com.zurich.extension.SPI;

import java.net.InetSocketAddress;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/21 20:20
 * @description
 */
@SPI
public interface ServiceRegistry {
    /**
     * 注册服务（服务端）
     *
     * @param rpcServiceName
     * @param inetSocketAddress
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
