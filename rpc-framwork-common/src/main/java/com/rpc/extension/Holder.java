package com.rpc.extension;

/**
 * 封装一个可变的、线程安全的泛型对象容器，主要用于在多线程环境中共享和传递可变对象引用
 * @param <T>
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
