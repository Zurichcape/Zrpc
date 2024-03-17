package com.zurich.serialize.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.zurich.exception.SerializeException;
import com.zurich.serialize.Serializer;
import com.zurich.remote.dto.RpcRequest;
import com.zurich.remote.dto.RpcResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2024/3/2 16:04
 * @description kryo的序列化速度很快，但是只兼容Java语言
 */
public class KryoSerializer implements Serializer {
    /**
     * Kryo是非线程安全的
     */
    private final ThreadLocal<Kryo> kryoThreadLocal = ThreadLocal.withInitial(()->{
        Kryo kryo = new Kryo();
        kryo.register(RpcResponse.class);
        kryo.register(RpcRequest.class);
        return kryo;
    });
    @Override
    public byte[] serialize(Object obj) {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Output output = new Output(byteArrayOutputStream)){
            Kryo kryo = new Kryo();
            kryo.writeObject(output,obj);
            return output.toBytes();
        }catch (Exception e){
            throw new SerializeException("Kryo Serialization failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
            Input input = new Input(byteArrayInputStream)){
            Kryo kryo = kryoThreadLocal.get();
            Object obj = kryo.readObject(input,clazz);
            kryoThreadLocal.remove();
            return clazz.cast(obj);
        }catch (Exception e){
            throw new SerializeException("Kryo Deserialization exception");
        }
    }
}
