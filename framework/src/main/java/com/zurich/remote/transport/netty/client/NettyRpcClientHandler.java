package com.zurich.remote.transport.netty.client;

import com.zurich.enums.CompressTypeEnum;
import com.zurich.enums.SerializationTypeEnum;
import com.zurich.factory.SingletonFactory;
import com.zurich.remote.constants.RpcConstants;
import com.zurich.remote.dto.RpcMessage;
import com.zurich.remote.dto.RpcResponse;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/27 22:29
 * @description 自定义client ChannelHandler来处理服务端发送的消息
 *  * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 *  * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。详见《Netty进阶之路 跟着案例学 Netty》
 */
@Slf4j
public class NettyRpcClientHandler extends ChannelInboundHandlerAdapter {
    private final UnprocessedRequests unprocessedRequests;
    private final NettyRpcClient nettyRpcClient;

    public NettyRpcClientHandler(){
        this.unprocessedRequests = SingletonFactory.getUniqueInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getUniqueInstance(NettyRpcClient.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        try{
            log.info("client receive msg: [{}]",msg);
            if(msg instanceof RpcMessage){
                RpcMessage tmp = (RpcMessage)msg;
                byte messageType = tmp.getMessageType();
                if(messageType == RpcConstants.HEARTBEAT_RESPONSE_TYPE){
                    log.info("heart [{}]",tmp.getData());
                }else if(messageType == RpcConstants.RESPONSE_TYPE){
                    RpcResponse<Object> rpcResponse =(RpcResponse<Object>) tmp.getData();
                    unprocessedRequests.complete(rpcResponse);
                }
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx,Object evt)throws Exception{
        if(evt instanceof IdleStateEvent){
            IdleState state =((IdleStateEvent) evt).state();
            if(state == IdleState.WRITER_IDLE){
                log.info("write idle happen [{}]",ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstants.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstants.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }else{
            super.userEventTriggered(ctx,evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception: ",cause);
        cause.printStackTrace();
        ctx.close();
    }
}
