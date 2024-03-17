package com.zurich.utils;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/22 11:28
 * @description
 */
public class RuntimeUtil {
    /**
     * 获取cpu的核心数
     * @return
     */
    public static int cpus(){
        return Runtime.getRuntime().availableProcessors();
    }
}
