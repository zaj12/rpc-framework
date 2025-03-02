package com.rpc.provider;

import com.rpc.config.RpcServiceConfig;

/**
 * 服务提供接口：存储和提供服务类
 */
public interface ServiceProvider {
    /**
     * 添加服务
     * @param rpcServiceConfig 服务相关的属性
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    /**
     * 获取服务
     * @param rpcServiceName 服务名
     * @return
     */
    Object getService(String rpcServiceName);

    /**
     * 发布服务
     * @param rpcServiceConfig
     */
    void publishService(RpcServiceConfig rpcServiceConfig);
}
