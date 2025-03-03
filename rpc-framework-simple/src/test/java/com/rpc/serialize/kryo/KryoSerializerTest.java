package com.rpc.serialize.kryo;

import com.rpc.remoting.dto.RpcRequest;
import com.rpc.serialize.hessian.HessianSerializer;
import com.rpc.serialize.kyro.KryoSerializer;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class KryoSerializerTest {
    @Test
    public void testKryoSerializer() {
        RpcRequest rpcRequest = RpcRequest.builder().methodName("hello")
                .parameters(new Object[]{"sayhelooloo", "sayhelooloosayhelooloo"})
                .interfaceName("com.rpc.HelloService")
                .paramTypes(new Class<?>[]{String.class, String.class})
                .requestId(UUID.randomUUID().toString())
                .group("group1")
                .version("version1")
                .build();

        KryoSerializer kryoSerializer = new KryoSerializer();
        byte[] bytes = kryoSerializer.serialize(rpcRequest);
        RpcRequest actual = kryoSerializer.deserialize(bytes, RpcRequest.class);
        assertEquals(rpcRequest.getGroup(), actual.getGroup());
        assertEquals(rpcRequest.getVersion(), actual.getVersion());
        assertEquals(rpcRequest.getRequestId(), actual.getRequestId());
    }
}
