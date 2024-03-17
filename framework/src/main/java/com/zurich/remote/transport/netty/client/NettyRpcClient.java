package com.zurich.remote.transport.netty.client;

import com.zurich.enums.CompressTypeEnum;
import com.zurich.enums.SerializationTypeEnum;
import com.zurich.enums.ServiceDiscoveryEnum;
import com.zurich.extension.ExtensionLoader;
import com.zurich.factory.SingletonFactory;
import com.zurich.discovery.ServiceDiscovery;
import com.zurich.remote.constants.RpcConstants;
import com.zurich.remote.dto.RpcMessage;
import com.zurich.remote.dto.RpcRequest;
import com.zurich.remote.dto.RpcResponse;
import com.zurich.remote.transport.RpcRequestTransport;
import com.zurich.remote.transport.netty.codec.RpcMessageDecoder;
import com.zurich.remote.transport.netty.codec.RpcMessageEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/27 22:28
 * @description
 */
@Slf4j
public class NettyRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;
    private final UnprocessedRequests unprocessedRequests;
    private final ChannelProvider channelProvider;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;

    public NettyRpcClient(){
        //初始化资源
        eventLoopGroup = new NioEventLoopGroup();
        //客户端启动类
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch){
                        ChannelPipeline p = ch.pipeline();
                        // 如果15秒内没有data发送给server,那么就会发送一个心跳包
                        p.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                        p.addLast(new RpcMessageEncoder());
                        p.addLast(new RpcMessageDecoder());
                        p.addLast(new NettyRpcClientHandler());
                    }
                });
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceDiscoveryEnum.ZK.getName());
        this.unprocessedRequests = SingletonFactory.getUniqueInstance(UnprocessedRequests.class);
        this.channelProvider = SingletonFactory.getUniqueInstance(ChannelProvider.class);
    }

    public Channel getChannel(InetSocketAddress inetSocketAddress){
        Channel channel = channelProvider.get(inetSocketAddress);
        if(null == channel){
            channel = doConnect(inetSocketAddress);
            channelProvider.set(inetSocketAddress,channel);
        }
        return channel;
    }
    public void close(){
        eventLoopGroup.shutdownGracefully();
    }

    @SneakyThrows
    public Channel doConnect(InetSocketAddress inetSocketAddress){
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        bootstrap.connect(inetSocketAddress).addListener((ChannelFutureListener)future->{
            if(future.isSuccess()){
                log.info("The client has connected [{}] successful!",inetSocketAddress.toString());
                completableFuture.complete(future.channel());
            }else{
                throw new IllegalStateException();
            }
        });
        return completableFuture.get();
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        CompletableFuture<RpcResponse<Object>> resFuture = new CompletableFuture<>();
        InetSocketAddress inetSocketAddress = serviceDiscovery.findService(rpcRequest);
        //获得channel相关的server address
        Channel channel = getChannel(inetSocketAddress);
        if(channel.isActive()){
            unprocessedRequests.put(rpcRequest.getRequestId(),resFuture);
            RpcMessage rpcMessage = RpcMessage.builder().data(rpcRequest)
                    .codec(SerializationTypeEnum.HESSIAN.getCode())
                    .compress(CompressTypeEnum.GZIP.getCode())
                    .messageType(RpcConstants.REQUEST_TYPE)
                    .build();
            channel.writeAndFlush(rpcMessage).addListener((ChannelFutureListener)future->{
                if(future.isSuccess()){
                    log.info("client send message [{}]",rpcMessage);
                }else{
                    future.channel().close();
                    resFuture.completeExceptionally(future.cause());
                    log.error("Send failed:",future.cause());
                }
            });
        }else{
            throw new IllegalStateException();
        }
        return resFuture;
    }
}
