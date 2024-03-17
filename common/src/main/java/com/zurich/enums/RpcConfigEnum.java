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
public enum RpcConfigEnum {
    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address"),
    ETCD_ADDRESS("rpc.etcd.address");
    private final String propertyValue;
}
