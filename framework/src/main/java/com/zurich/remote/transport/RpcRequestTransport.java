package com.zurich.remote.transport;
import com.zurich.extension.SPI;
import com.zurich.remote.dto.RpcRequest;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/20 23:56
 * @description rpc请求传输接口
 */
@SPI
public interface RpcRequestTransport {
    /**
     * send rpc request to server and result
     * @param rpcRequest
     * @return data from server
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
