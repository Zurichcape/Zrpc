package com.zurich.config;

import com.zurich.utils.CuratorUtils;
import com.zurich.remote.transport.netty.server.NettyRpcServer;
import com.zurich.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/12/1 21:54
 * @description 当服务端关闭后的处理钩子，执行比如注销所有服务的操作
 */
@Slf4j
public class CustomShutdownHook {
    private CustomShutdownHook(){};

    private static final CustomShutdownHook CUSTOM_SHUTDOWN_HOOK = new CustomShutdownHook();

    public static CustomShutdownHook getCustomShutdownHook(){
        return CUSTOM_SHUTDOWN_HOOK;
    }

    public void clearAll(){
        log.info("addShutdownHook for clearAll");
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost()
                        .getHostAddress(), NettyRpcServer.PORT);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(),inetSocketAddress);
            }catch (UnknownHostException ignored){
            }
            ThreadPoolFactoryUtil.shutdownAllThreadPool();
        }));
    }
}
