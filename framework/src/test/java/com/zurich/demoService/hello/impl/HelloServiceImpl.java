package com.zurich.demoService.hello.impl;

import com.zurich.annotation.RpcService;
import com.zurich.demoService.hello.HelloService;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2024/3/2 20:15
 * @description
 */
@RpcService(group = "g1",version = "v1.1")
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello() {
        return "hello world";
    }
}
