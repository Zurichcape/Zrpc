package com.zurich.serialize;

import com.zurich.extension.SPI;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/27 21:29
 * @description 序列化接口，所有序列化类均需要实现该接口
 */
@SPI
public interface Serializer {
    /**
     * 序列化方法
     * @param obj 序列化对象
     * @return 字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 反序列化方法
     * @param bytes 序列化后的字节数组
     * @param clazz 目标类
     * @param <T> 类的类型 example {@code String.class} 的类型是 {@code Class<String>}.
     * @return 反序列化的对象
     */
    <T> T deserialize(byte[] bytes,Class<T> clazz);
}
