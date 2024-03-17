package com.zurich.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/20 22:25
 * @description
 */
@AllArgsConstructor
@Getter
public enum RpcRequestTransportEnum {
    NETTY("netty"),
    SOCKET("socket");
    private final String name;
}
