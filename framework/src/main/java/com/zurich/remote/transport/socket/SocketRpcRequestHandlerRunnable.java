package com.zurich.remote.transport.socket;

import com.zurich.factory.SingletonFactory;
import com.zurich.remote.dto.RpcRequest;
import com.zurich.remote.dto.RpcResponse;
import com.zurich.remote.handler.RpcRequestHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.ThrowsAdvice;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/12/2 10:32
 * @description
 */
@Slf4j
public class SocketRpcRequestHandlerRunnable implements Runnable{
    private final Socket socket;
    private final RpcRequestHandler rpcRequestHandler;

    public SocketRpcRequestHandlerRunnable(Socket socket){
        this.socket = socket;
        this.rpcRequestHandler = SingletonFactory.getUniqueInstance(RpcRequestHandler.class);
    }

    @Override
    public void run(){
        log.info("server handle message from client by thread: {}", Thread.currentThread().getName());
        try(ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream())){
            RpcRequest rpcRequest = (RpcRequest) objectInputStream.readObject();
            Object result = rpcRequestHandler.handle(rpcRequest);
            objectOutputStream.writeObject(RpcResponse.success(result,rpcRequest.getRequestId()));
            objectOutputStream.flush();
        }catch (IOException | ClassNotFoundException e){
            log.error("exception occur:", e);
        }
    }
}
