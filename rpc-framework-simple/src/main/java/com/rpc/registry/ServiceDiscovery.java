package com.rpc.registry;

import com.rpc.extension.SPI;
import com.rpc.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * 服务发现
 */
@SPI
public interface ServiceDiscovery {
    /**
     * 根据 rpcServiceName 获取远程服务地址
     * @param rpcRequest 服务请求
     * @return
     */
    InetSocketAddress lookupService(RpcRequest rpcRequest);
}
