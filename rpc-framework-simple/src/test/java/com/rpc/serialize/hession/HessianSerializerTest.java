package com.rpc.serialize.hession;

import com.rpc.remoting.dto.RpcRequest;
import com.rpc.serialize.hessian.HessianSerializer;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class HessianSerializerTest {
    @Test
    public void hessianSerializerTest() {
        RpcRequest rpcRequest = RpcRequest.builder().methodName("hello")
                .parameters(new Object[]{"sayhelooloo", "sayhelooloosayhelooloo"})
                .interfaceName("com.rpc.HelloService")
                .paramTypes(new Class<?>[]{String.class, String.class})
                .requestId(UUID.randomUUID().toString())
                .group("group1")
                .version("version1")
                .build();

        HessianSerializer hessianSerializer = new HessianSerializer();
        byte[] bytes = hessianSerializer.serialize(rpcRequest);
        RpcRequest actual = hessianSerializer.deserialize(bytes, RpcRequest.class);
        assertEquals(rpcRequest.getGroup(), actual.getGroup());
        assertEquals(rpcRequest.getVersion(), actual.getVersion());
        assertEquals(rpcRequest.getRequestId(), actual.getRequestId());
    }
}
