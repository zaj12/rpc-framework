package com.rpc.serializer;

public interface Serializer {
    /**
     * 将对象序列化为字节数组
     *
     * @param obj 需要序列化的对象
     * @return 序列化后的字节数组
     */
    byte[] serialize(Object obj);

    /**
     * 将字节数组反序列化为对象
     *
     * @param bytes 字节数组
     * @param clazz 目标对象的类类型
     * @param <T>   目标对象的类型
     * @return 反序列化后的对象
     */
    <T> T deserialize(byte[] bytes, Class<T> clazz);
}