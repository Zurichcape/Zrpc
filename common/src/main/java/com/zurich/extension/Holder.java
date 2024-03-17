package com.zurich.extension;


/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/21 19:01
 * @description
 */
public class Holder<T> {
    private volatile T value;

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }
}
