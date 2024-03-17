package com.zurich.serialize.protostuff;

import com.zurich.serialize.Serializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2024/3/2 15:47
 * @description
 */
public class ProtoStuffSerializer implements Serializer {
    /**
     * 避免每次序列化都需要重新分配buffer
     */
    private static final LinkedBuffer BUFFER = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
    @Override
    public byte[] serialize(Object obj) {
        Schema schema = RuntimeSchema.getSchema(obj.getClass());
        byte[] bytes;
        try{
            bytes = ProtostuffIOUtil.toByteArray(obj,schema,BUFFER);
        }finally {
            BUFFER.clear();
        }
        return bytes;
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T obj =schema.newMessage();
        ProtostuffIOUtil.mergeFrom(bytes,obj,schema);
        return obj;
    }
}
