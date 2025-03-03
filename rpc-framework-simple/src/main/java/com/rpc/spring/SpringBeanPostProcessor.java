package com.rpc.spring;

import com.rpc.annotation.RpcReference;
import com.rpc.annotation.RpcService;
import com.rpc.config.RpcServiceConfig;
import com.rpc.enums.RpcRequestTransportEnum;
import com.rpc.extension.ExtensionLoader;
import com.rpc.factory.SingletonFactory;
import com.rpc.provider.ServiceProvider;
import com.rpc.provider.impl.ZKServiceProviderImpl;
import com.rpc.proxy.RpcClientProxy;
import com.rpc.remoting.transport.RpcRequestTransport;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;


/**
 * 在创建 bean 之前调用此方法，以查看类是否带有注解
 */

@Slf4j
@Component
public class SpringBeanPostProcessor implements BeanPostProcessor {
    private final ServiceProvider serviceProvider;
    private final RpcRequestTransport rpcClient;

    public SpringBeanPostProcessor() {
        this.serviceProvider = SingletonFactory.getInstance(ZKServiceProviderImpl.class);
        this.rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class)
                .getExtension(RpcRequestTransportEnum.NETTY.getName());
    }

    /**
     * Spring bean 在实例化之前会调用 postProcessBeforeInitialization() 方法
     * 方法中去判断类上是否有 RpcService 注解。如果有的话，就取出 group 和 version 的值。
     * 然后，再调用 ServiceProvider 的 publishService() 方法发布服务即可
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @SneakyThrows
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean.getClass().isAnnotationPresent(RpcService.class)) {
            log.info("[{}] is annotated with [{}]", bean.getClass().getName(), RpcService.class.getCanonicalName());
            // 获取RpcService注解
            RpcService rpcService = bean.getClass().getAnnotation(RpcService.class);
            // 获取group和version，来注册服务
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                    .group(rpcService.group())
                    .version(rpcService.version())
                    .service(bean)
                    .build();
            serviceProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    /**
     * 在 Spring bean 实例化之后会调用 postProcessAfterInitialization() 方法
     * 方法中遍历类的属性上是否有 RpcReference 注解。
     * 如果有的话，我们就通过反射将这个属性赋值即可
     * @param bean
     * @param beanName
     * @return
     * @throws BeansException
     */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            RpcReference rpcReference = declaredField.getAnnotation(RpcReference.class);
            if (rpcReference != null) {
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                        .group(rpcReference.group())
                        .version(rpcReference.version())
                        .build();
                RpcClientProxy rpcClientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                Object clientProxy = rpcClientProxy.getProxy(declaredField.getType());
                declaredField.setAccessible(true);
                try {
                    // 通过反射将代理对象设置到字段中，后续对该字段的调用会触发远程RPC
                    declaredField.set(bean, clientProxy);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
