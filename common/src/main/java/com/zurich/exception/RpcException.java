package com.zurich.exception;

import com.zurich.enums.RpcErrorMessageEnum;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/23 20:41
 * @description
 */
public class RpcException extends RuntimeException{
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum,String detail){
        super(rpcErrorMessageEnum.getMessage()+":"+detail);
    }
    public RpcException(String message,Throwable cause){
        super(message,cause);
    }
    public RpcException(RpcErrorMessageEnum rpcErrorMessageEnum){
        super(rpcErrorMessageEnum.getMessage());
    }
}
