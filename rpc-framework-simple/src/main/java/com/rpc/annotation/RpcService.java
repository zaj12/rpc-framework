package com.rpc.annotation;

import java.lang.annotation.*;

/**
 * RPC 服务注解，标记服务实现类
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface RpcService {
    String version() default "";

    String group() default "";
}
