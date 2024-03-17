package com.zurich.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/20 22:26
 * @description
 */
@AllArgsConstructor
@Getter
public enum CompressTypeEnum {
    GZIP((byte) 0x01, "gzip");

    private final byte code;
    private final String name;

    public static String getName(byte code) {
        for (CompressTypeEnum c : CompressTypeEnum.values()) {
            if (c.getCode() == code) {
                return c.name;
            }
        }
        return null;
    }
}
