package com.zurich.remote.dto;

import lombok.*;

import java.io.Serializable;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/20 22:03
 * @description
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
    private static final long serialVersionUID = 1905122041950251207L;
    private String requestId;
    private String interfaceName;
    private String methodName;
    private Object[] parameters;
    private Class<?>[] paramTypes;
    private String version;
    private String group;

    public String getRpcServiceName(){return this.getInterfaceName()+"-"+this.getGroup()+"-"+this.getVersion();}
}
