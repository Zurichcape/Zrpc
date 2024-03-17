package com.zurich.pojo;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/23 0:28
 * @description
 */

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户
 */
@AllArgsConstructor
@Setter
@Getter
public class User {

    private String name;
    private Address address;

    // constructors, getters and setters

}


