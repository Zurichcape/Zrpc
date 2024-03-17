package com.zurich.proxy;

import com.zurich.config.RpcServiceConfig;
import com.zurich.enums.RpcErrorMessageEnum;
import com.zurich.enums.RpcResponseCodeEnum;
import com.zurich.exception.RpcException;
import com.zurich.remote.dto.RpcRequest;
import com.zurich.remote.dto.RpcResponse;
import com.zurich.remote.transport.RpcRequestTransport;
import com.zurich.remote.transport.netty.client.NettyRpcClient;
import com.zurich.remote.transport.socket.SocketRpcClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/20 21:59
 * @description
 */
@Slf4j
public class RpcClientProxy implements InvocationHandler {
private static final String INTERFACE_NAME = "interfaceName";
    /**
     * 用于发送请求给服务端，这里我给出了两种实现，netty和socket
     */
    private final RpcRequestTransport rpcRequestTransport;
    private final RpcServiceConfig rpcServiceConfig;
    public RpcClientProxy(RpcRequestTransport rpcRequestTransport, RpcServiceConfig rpcServiceConfig){
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = rpcServiceConfig;
    }

    public RpcClientProxy(RpcRequestTransport rpcRequestTransport){
        this.rpcRequestTransport = rpcRequestTransport;
        this.rpcServiceConfig = new RpcServiceConfig();
    }

    /**
     * 获取该类的代理类
     * @param clazz
     * @param <T>
     * @return T 代理类
     */
    @SuppressWarnings("unchecked")
    public<T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class<?>[]{clazz},this);
    }

    /**
     * 使用动态代理进行方法调用
     * @param proxy
     * @param method
     * @param args
     * @return Object
     */
    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        log.info("invoke method: [{}]",method.getName());
        RpcRequest rpcRequest = RpcRequest.builder().methodName(method.getName())
                .parameters(args)
                .interfaceName(method.getDeclaringClass().getName())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        RpcResponse<Object> rpcResponse = null;
        if(rpcRequestTransport instanceof NettyRpcClient){
            CompletableFuture<RpcResponse<Object>> completeFuture = (CompletableFuture<RpcResponse<Object>>)rpcRequestTransport.sendRpcRequest(rpcRequest);
            rpcResponse = completeFuture.get();
        }
        if(rpcRequestTransport instanceof SocketRpcClient){
            rpcResponse = (RpcResponse<Object>)rpcRequestTransport.sendRpcRequest(rpcRequest);
        }
        this.check(rpcResponse,rpcRequest);
        return rpcResponse.getData();
     }

    /**
     * 完整性检查
     * @param rpcResponse
     * @param rpcRequest
     */
     private void check(RpcResponse<Object> rpcResponse,RpcRequest rpcRequest){
        if(rpcResponse == null){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_INVOCATION_FAILURE,INTERFACE_NAME+":"+rpcRequest.getInterfaceName());
        }
        if(!rpcResponse.getCode().equals(RpcResponseCodeEnum.SUCCESS.getCode())){
            throw new RpcException(RpcErrorMessageEnum.CLIENT_CONNECT_SERVER_FAILURE,INTERFACE_NAME+":"+rpcRequest.getInterfaceName());
        }
        if(!rpcResponse.getRequestId().equals(rpcRequest.getRequestId())){
            throw new RpcException(RpcErrorMessageEnum.REQUEST_NOT_MATCH_RESPONSE,INTERFACE_NAME+":"+rpcRequest.getInterfaceName());
        }
     }
}
