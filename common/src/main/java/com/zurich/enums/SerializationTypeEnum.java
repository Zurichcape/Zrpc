package com.zurich.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/20 22:25
 * @description
 */
@AllArgsConstructor
@Getter
public enum SerializationTypeEnum {
    KYRO((byte) 0x01, "kyro"),
    PROTOSTUFF((byte) 0x02, "protostuff"),
    HESSIAN((byte) 0X03, "hessian");

    private final Byte code;
    private final String name;

    public static String getName(byte code){
        for(SerializationTypeEnum e : SerializationTypeEnum.values()){
            if(e.getCode() == code){
                return e.name;
            }
        }
        return null;
    }
}
