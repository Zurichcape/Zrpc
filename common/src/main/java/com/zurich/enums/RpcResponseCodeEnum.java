package com.zurich.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/20 22:25
 * @description
 */
@AllArgsConstructor
@Getter
@ToString
public enum RpcResponseCodeEnum {
    SUCCESS(200, "The remote call is successful"),
    FAIL(500, "The remote call is fail");
    private final Integer code;
    private final String message;
}
