package com.rpc.registry;

import com.rpc.DemoRpcServiceImpl;
import com.rpc.config.RpcServiceConfig;
import com.rpc.registry.zk.ZKServiceDiscoveryImpl;
import com.rpc.registry.zk.ZKServiceRegistryImpl;
import com.rpc.remoting.dto.RpcRequest;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ZKServiceRegistryImplTest {
    /**
     * 测试会报错rpc.properties 文件读取失败。
     * rpc.properties是用于用户配置zk客户端地址的（若没有则使用默认地址127.0.0.1:2181），这里没有定义自然会报错。
     */
    @Test
    public void should_register_service_successful_and_lookup_service_by_service_name() {
        ZKServiceRegistryImpl zkServiceRegistry = new ZKServiceRegistryImpl();
        InetSocketAddress givenInetSocketAddress = new InetSocketAddress("127.0.0.1", 9333);
        DemoRpcServiceImpl demoRpcService = new DemoRpcServiceImpl();
        RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                .group("test2").version("version2").service(demoRpcService).build();
        zkServiceRegistry.registerService(rpcServiceConfig.getRpcServiceName(), givenInetSocketAddress);

        ZKServiceDiscoveryImpl zkServiceDiscovery = new ZKServiceDiscoveryImpl();
        RpcRequest rpcRequest = RpcRequest.builder()
                .interfaceName(rpcServiceConfig.getServiceName())
                .requestId(UUID.randomUUID().toString())
                .group(rpcServiceConfig.getGroup())
                .version(rpcServiceConfig.getVersion())
                .build();
        InetSocketAddress acquiredInetSocketAddress = zkServiceDiscovery.lookupService(rpcRequest);
        assertEquals(givenInetSocketAddress.toString(), acquiredInetSocketAddress.toString());
    }
}
