package com.zurich.remote.transport.socket;

import com.zurich.config.CustomShutdownHook;
import com.zurich.config.RpcServiceConfig;
import com.zurich.factory.SingletonFactory;
import com.zurich.provider.ServiceProvider;
import com.zurich.provider.impl.ZkServiceProviderImpl;
import com.zurich.remote.transport.netty.server.NettyRpcServer;
import com.zurich.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/12/2 10:32
 * @description
 */
@Slf4j
public class SocketRpcServer {
    private final ExecutorService threadPool;
    private final ServiceProvider serviceProvider;

    public SocketRpcServer(){
        this.threadPool = ThreadPoolFactoryUtil.createCustomThreadPoolIfAbsent("socket-server-rpc-pool");
        serviceProvider = SingletonFactory.getUniqueInstance(ZkServiceProviderImpl.class);
    }

    public void registerService(RpcServiceConfig rpcServiceConfig){
        serviceProvider.publishService(rpcServiceConfig);
    }

    public void start(){
        try(ServerSocket server = new ServerSocket()){
            String host = InetAddress.getLocalHost().getHostAddress();
            server.bind(new InetSocketAddress(host, NettyRpcServer.PORT));
            CustomShutdownHook.getCustomShutdownHook().clearAll();
            Socket socket;
            while((socket = server.accept())!= null){
                log.info("client connected [{}]",socket.getInetAddress());
                threadPool.execute(new SocketRpcRequestHandlerRunnable(socket));
            }
            threadPool.shutdown();
        }catch (IOException e){
            log.error("IOException occur:",e);
        }
    }
}
