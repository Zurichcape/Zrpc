package com.zurich.loadbalance;

import com.zurich.extension.SPI;
import com.zurich.remote.dto.RpcRequest;

import java.util.List;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/23 23:36
 * @description 负载均衡接口
 */
@SPI
public interface LoadBalance {
    /**
     * 服务发现选择负载均衡算法
     * @param serviceUrlList
     * @param rpcRequest
     * @return String
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
