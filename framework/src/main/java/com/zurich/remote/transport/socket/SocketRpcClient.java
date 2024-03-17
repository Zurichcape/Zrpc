package com.zurich.remote.transport.socket;

import com.zurich.enums.ServiceDiscoveryEnum;
import com.zurich.exception.RpcException;
import com.zurich.extension.ExtensionLoader;
import com.zurich.discovery.ServiceDiscovery;
import com.zurich.remote.dto.RpcRequest;
import com.zurich.remote.transport.RpcRequestTransport;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/12/2 10:21
 * @description 基于 Socket 传输 RpcRequest
 */
@AllArgsConstructor
@Slf4j
public class SocketRpcClient implements RpcRequestTransport {
    private final ServiceDiscovery serviceDiscovery;

    public SocketRpcClient(){
        this.serviceDiscovery = ExtensionLoader.getExtensionLoader(ServiceDiscovery.class).getExtension(ServiceDiscoveryEnum.ZK.getName());
    }

    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        InetSocketAddress inetSocketAddress = serviceDiscovery.findService(rpcRequest);
        try(Socket socket = new Socket()){
            socket.connect(inetSocketAddress);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            //通过输出流给服务端发送数据
            objectOutputStream.writeObject(rpcRequest);
            //通过输入流读取返回数据
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            return objectInputStream.readObject();
        }catch (IOException | ClassNotFoundException e){
            throw new RpcException("调用服务失败: ", e);
        }
    }
}
