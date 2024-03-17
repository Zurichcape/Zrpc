package com.zurich.remote.transport.netty.client;

import com.zurich.remote.dto.RpcResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/27 22:29
 * @description 服务端未处理的请求
 */
public class UnprocessedRequests {
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public void put(String requestId, CompletableFuture<RpcResponse<Object>>future){
        UNPROCESSED_RESPONSE_FUTURES.put(requestId,future);
    }

    public void complete(RpcResponse<Object> rpcResponse){
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if(null != future){
            future.complete(rpcResponse);
        }else{
            throw new IllegalStateException();
        }
    }
}
