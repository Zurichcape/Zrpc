package com.zurich.compress.gzip;

import com.zurich.compress.Compress;
import com.zurich.serialize.kryo.KryoSerializer;
import com.zurich.remote.dto.RpcRequest;
import org.junit.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2024/3/2 17:16
 * @description
 */
public class GzipCompressTest {
    @Test
    public void gzipCompressTest(){
        Compress compress = new GzipCompress();
        RpcRequest rpcRequest = RpcRequest.builder().methodName("hello")
                .parameters(new Object[]{"say hello","coder man"})
                .interfaceName("com.zurich.HelloService")
                .paramTypes(new Class<?>[]{String.class,String.class})
                .requestId(UUID.randomUUID().toString())
                .group("g1")
                .version("1.0")
                .build();
        KryoSerializer kryoSerializer = new KryoSerializer();
        byte[] rpcRequestBytes = kryoSerializer.serialize(rpcRequest);
        byte[] compressRpcRequestBytes = compress.compress(rpcRequestBytes);
        byte[] decompressRpcRequestBytes = compress.decompress(compressRpcRequestBytes);
        assertEquals(rpcRequestBytes.length,decompressRpcRequestBytes.length);
    }
}
