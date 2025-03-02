package com.rpc.registry;

import com.rpc.extension.SPI;

import java.net.InetSocketAddress;

/**
 * 服务注册
 */
@SPI
public interface ServiceRegistry {
    /**
     * 注册服务到注册中心
     * @param rpcServiceName 完整的服务名称（class name+group+version）
     * @param inetSocketAddress 远程服务地址
     */
    void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress);
}
