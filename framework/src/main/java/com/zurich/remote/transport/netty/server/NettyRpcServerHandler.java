package com.zurich.remote.transport.netty.server;

import com.zurich.enums.CompressTypeEnum;
import com.zurich.enums.RpcResponseCodeEnum;
import com.zurich.enums.SerializationTypeEnum;
import com.zurich.factory.SingletonFactory;
import com.zurich.remote.constants.RpcConstants;
import com.zurich.remote.dto.RpcMessage;
import com.zurich.remote.dto.RpcRequest;
import com.zurich.remote.dto.RpcResponse;
import com.zurich.remote.handler.RpcRequestHandler;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import io.protostuff.Rpc;
import lombok.extern.slf4j.Slf4j;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/12/1 23:12
 * @description 自定义实现服务端的ChannelHandler来处理客户端发送的数据
 * 如果继承自 SimpleChannelInboundHandler 的话就不要考虑 ByteBuf 的释放 ，{@link SimpleChannelInboundHandler} 内部的
 * channelRead 方法会替你释放 ByteBuf ，避免可能导致的内存泄露问题。
 */
@Slf4j
public class NettyRpcServerHandler extends ChannelInboundHandlerAdapter {
    private final RpcRequestHandler rpcRequestHandler;

    public NettyRpcServerHandler(){
        this.rpcRequestHandler = SingletonFactory.getUniqueInstance(RpcRequestHandler.class);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg){
        try{
            if(msg instanceof RpcMessage){
                log.info("server receive msg: [{}]", msg);
                byte messageType = ((RpcMessage) msg).getMessageType();
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.HESSIAN.getCode());
                rpcMessage.setCompress(CompressTypeEnum.GZIP.getCode());
                if(messageType == RpcConstants.HEARTBEAT_REQUEST_TYPE){
                    rpcMessage.setMessageType(RpcConstants.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstants.PONG);
                }else{
                    RpcRequest rpcRequest = (RpcRequest)((RpcMessage) msg).getData();
                    //执行目标方法，即客户端调用的方法，并返回执行结果
                    Object result = rpcRequestHandler.handle(rpcRequest);
                    log.info(String.format("server get result: %s", result.toString()));
                    rpcMessage.setMessageType(RpcConstants.RESPONSE_TYPE);
                    if(ctx.channel().isActive() && ctx.channel().isWritable()){
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result,rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                    }else{
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.error("not writable now, message dropped");
                    }
                    ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                }
            }
        }finally {
            //保证ByteBuf已经释放了，否则会发生内存泄露
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent)evt).state();
            if(state == IdleState.READER_IDLE){
                log.info("idle check happen, so close the connection");
                ctx.close();
            }else{
                super.userEventTriggered(ctx,evt);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
