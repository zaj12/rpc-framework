package com.rpc.extension;

import java.lang.annotation.*;


/**
 * 用于标记扩展点接口（如 Dubbo 框架中的用法），
 * 表明该接口允许通过 SPI（Service Provider Interface）机制动态加载实现类
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface SPI {
}
