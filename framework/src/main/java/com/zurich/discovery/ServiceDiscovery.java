package com.zurich.discovery;

import com.zurich.extension.SPI;
import com.zurich.remote.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/23 23:32
 * @description 服务发现接口
 */
@SPI
public interface ServiceDiscovery {
    /**
     * 服务发现
     * @param rpcRequest
     * @return InetSocketAddress
     */
    InetSocketAddress findService(RpcRequest rpcRequest);
}
