package com.zurich.spring;

import com.zurich.annotation.RpcReference;
import com.zurich.annotation.RpcService;
import com.zurich.config.RpcServiceConfig;
import com.zurich.enums.RpcRequestTransportEnum;
import com.zurich.extension.ExtensionLoader;
import com.zurich.factory.SingletonFactory;
import com.zurich.provider.ServiceProvider;
import com.zurich.provider.impl.ZkServiceProviderImpl;
import com.zurich.proxy.RpcClientProxy;
import com.zurich.remote.transport.RpcRequestTransport;
import com.zurich.remote.transport.netty.client.NettyRpcClient;
import io.protostuff.Rpc;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.C;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/21 18:58
 * @description 自定义Bean后处理器
 */
@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor(){
        this.serviceProvider = SingletonFactory.getUniqueInstance(ZkServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension(RpcRequestTransportEnum.NETTY.getName());
    }

    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean.getClass().isAnnotationPresent(RpcService.class)){
            log.info("[{}]  is annotated with [{}]",bean.getClass().getName(),RpcService.class.getCanonicalName());
        }
        //构建rpc服务配置
        RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
        RpcServiceConfig rpcServiceConfig =RpcServiceConfig.builder()
                .group(rpcService.group())
                .version(rpcService.version())
                .service(bean).build();
        //注册服务
        serviceProvider.publishService(rpcServiceConfig);
        return bean;
    }

    @SneakyThrows
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for(Field field : declaredFields){
            RpcReference rpcReference = field.getAnnotation(RpcReference.class);
            if(null != rpcReference){
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version()).build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient,rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(field.getType());
                field.setAccessible(true);
                try{
                    field.set(bean,clientProxy);
                }catch (IllegalAccessException e){
                    e.printStackTrace();
                }
            }

        }
        return bean;
    }
}
