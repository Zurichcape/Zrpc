package com.zurich.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/23 21:15
 * @description 获取单例的工厂类
 */
import java.lang.reflect.Constructor;

public class SingletonFactory {

    /**
     * 私有静态内部类，用于存放Singleton实例
     */
    private static class SingletonHolder<T> {
        private static final Map<String, Object> INSTANCES = new ConcurrentHashMap<>();

        private SingletonHolder() {}

        // 提供获取单例的方法
        public static <T> T getInstance(Class<T> clazz) {
            if (clazz == null) {
                throw new IllegalArgumentException("Class must not be null");
            }

            String key = clazz.getName();
            // 对整个map加锁，确保在并发环境中创建单例的线程安全
            synchronized (INSTANCES) {
                if (!INSTANCES.containsKey(key)) {
                    try {
                        Constructor<T> constructor = clazz.getDeclaredConstructor();
                        constructor.setAccessible(true); // 允许访问私有构造函数
                        T instance = constructor.newInstance();
                        INSTANCES.put(key, instance);
                        return instance;
                    } catch (ReflectiveOperationException e) {
                        throw new RuntimeException("Failed to create an instance of class " + clazz.getName(), e);
                    }
                }
            }

            return clazz.cast(INSTANCES.get(key));
        }
    }

    /**
     * 外部获取单例的公共方法
     */
    public static <T> T getUniqueInstance(Class<T> clazz) {
        return SingletonHolder.getInstance(clazz);
    }

    /**
     * 确保不能通过new关键词实例化SingletonFactory
     */
    private SingletonFactory() {}
}