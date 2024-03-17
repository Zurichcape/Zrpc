package com.zurich.remote.dto;

import lombok.*;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/20 22:03
 * @description
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
@ToString
public class RpcMessage {
    /**
     * 消息类型
     */
    private byte messageType;
    /**
     * 序列化方式
     */
    private byte codec;
    /**
     * 压缩方式
     */
    private byte compress;
    /**
     * 请求id
     */
    private int requestId;
    /**
     * 请求数据主体
     */
    private Object data;
}
