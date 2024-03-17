package com.zurich.utils;

import java.util.Collection;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/22 11:38
 * @description 集合工具类
 */
public class CollectionUtil {
    public static boolean isEmpty(Collection<?> c){return c==null || c.isEmpty();}
}
