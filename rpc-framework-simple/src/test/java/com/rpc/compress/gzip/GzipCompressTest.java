package com.rpc.compress.gzip;

import com.rpc.compress.Compress;
import com.rpc.remoting.dto.RpcRequest;
import com.rpc.serialize.kyro.KryoSerializer;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

public class GzipCompressTest {
    private static final Logger log = LoggerFactory.getLogger(GzipCompressTest.class);

    /**
     * 测试不关心 RpcRequest 中字段的实际含义（如 interfaceName 是否指向真实接口），只验证数据完整性
     */
    @Test
    public void gzipCompressTest() {
        Compress gzipCompress = new GzipCompress();
        RpcRequest rpcRequest = RpcRequest.builder().methodName("hello")
                .parameters(new Object[]{"sayhelooloo", "sayhelooloosayhelooloo"})
                .interfaceName("com.rpc.HelloService")
                .paramTypes(new Class<?>[]{String.class, String.class})
                .requestId(UUID.randomUUID().toString())
                .group("group1")
                .version("version1")
                .build();
        if (rpcRequest == null) {
            log.info("rpcRequest is null");
        }
        KryoSerializer kryoSerializer = new KryoSerializer();
        byte[] rpcRequestBytes = kryoSerializer.serialize(rpcRequest);
        byte[] compressRpcRequestBytes = gzipCompress.compress(rpcRequestBytes);
        byte[] decompressRpcRequestBytes = gzipCompress.decompress(compressRpcRequestBytes);
        assertEquals(rpcRequestBytes.length, decompressRpcRequestBytes.length);
        assertArrayEquals(rpcRequestBytes, decompressRpcRequestBytes);
    }
}
