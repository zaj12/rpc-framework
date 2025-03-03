package com.rpc.annotation;

import java.lang.annotation.*;

/**
 * RPC 引用注解，自动装配服务实现类
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Inherited
public @interface RpcReference {
    String version() default "";

    String group() default "";
}
