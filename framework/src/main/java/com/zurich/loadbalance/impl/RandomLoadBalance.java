package com.zurich.loadbalance.impl;

import com.zurich.loadbalance.AbstractLoadBalance;
import com.zurich.remote.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Random;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/23 23:39
 * @description
 */
@Slf4j
public class RandomLoadBalance extends AbstractLoadBalance {
    @Override
    protected String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest) {
        Random random = new Random();
        return serviceUrlList.get(random.nextInt(serviceUrlList.size()));
    }
}
