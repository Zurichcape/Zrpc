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
public enum LoadBalanceEnum {
    LOAD_BALANCE("loadBalance");
    private final String name;
}
