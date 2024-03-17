package com.zurich.remote.handler;

import com.zurich.exception.RpcException;
import com.zurich.factory.SingletonFactory;
import com.zurich.provider.ServiceProvider;
import com.zurich.provider.impl.ZkServiceProviderImpl;
import com.zurich.remote.dto.RpcRequest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/21 18:44
 * @description RpcRequest
 */
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;

    public RpcRequestHandler(){
        serviceProvider = SingletonFactory.getUniqueInstance(ZkServiceProviderImpl.class);
    }

    /**
     * @param rpcRequest client request
     * @return 返回远程过程调用执行结果
     */
    public Object handle(RpcRequest rpcRequest){
        Object service = serviceProvider.getService(rpcRequest.getRpcServiceName());
        return invokeTargetMethod(rpcRequest,service);
    }

    /**
     * 获取远程调用方法结果
     * @param rpcRequest client request
     * @param service    service object
     * @return the result of the target method execution
     */
    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service){
        Object result;
        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName(),rpcRequest.getParamTypes());
            result = method.invoke(service,rpcRequest.getParameters());
        }catch (NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException e){
            throw new RpcException(e.getMessage(),e);
        }
        return result;
    }
}
