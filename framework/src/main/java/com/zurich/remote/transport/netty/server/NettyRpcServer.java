package com.zurich.remote.transport.netty.server;

import com.zurich.config.CustomShutdownHook;
import com.zurich.config.RpcServiceConfig;
import com.zurich.provider.impl.ZkServiceProviderImpl;
import com.zurich.factory.SingletonFactory;
import com.zurich.provider.ServiceProvider;
import com.zurich.remote.transport.netty.codec.RpcMessageDecoder;
import com.zurich.remote.transport.netty.codec.RpcMessageEncoder;
import com.zurich.utils.RuntimeUtil;
import com.zurich.utils.concurrent.threadpool.ThreadPoolFactoryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/12/1 21:24
 * @description 服务端，根据客户端信息调用相应的方法并且返回结果给客户端
 */
@Slf4j
@Component
public class NettyRpcServer {
    public static final int PORT = 9999;

    private static final ServiceProvider SERVICE_PROVIDER = SingletonFactory.getUniqueInstance(ZkServiceProviderImpl.class);

    public void registerService(RpcServiceConfig rpcServiceConfig){
        SERVICE_PROVIDER.publishService(rpcServiceConfig);
    }

    @SneakyThrows
    public void start(){
        CustomShutdownHook.getCustomShutdownHook().clearAll();
        String host = InetAddress.getLocalHost().getHostAddress();
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        DefaultEventExecutorGroup serviceHandlerGroup = new DefaultEventExecutorGroup(
                RuntimeUtil.cpus()*2,
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group",false)
        );
        try{
            //服务端启动类
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    // TCP默认开启了 Nagle 算法，该算法的作用是尽可能的发送大数据快，减少网络传输。
                    // TCP_NODELAY 参数的作用就是控制是否启用 Nagle 算法。
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    // 是否开启了TCP底层保活机制，即心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    // 表示系统用于临时存放已完成三次握手的请求的队列的最大长度,
                    // 如果连接建立频繁服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG,128)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    // 当客户端第一次进行请求的时候才会进行初始化
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch){
                            // 30秒内没有收客户端请求则关闭连接
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));
                            p.addLast(new RpcMessageEncoder());
                            p.addLast(new RpcMessageDecoder());
                            p.addLast(serviceHandlerGroup,new NettyRpcServerHandler());
                        }
                    });
            // 绑定端口，同步等待绑定成功
            ChannelFuture f = b.bind(host,PORT).sync();
            // 等待服务器监听端口关闭
            f.channel().closeFuture().sync();
        }catch (InterruptedException e){
            log.error("occur exception when start server: ",e);
        }finally {
            log.error("shutdown bossGroup and workerGroup");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            serviceHandlerGroup.shutdownGracefully();
        }

    }
}
