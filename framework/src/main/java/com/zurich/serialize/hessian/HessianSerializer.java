package com.zurich.serialize.hessian;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.zurich.exception.SerializeException;
import com.zurich.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2024/3/2 16:32
 * @description
 */
public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        try(ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()){
            HessianOutput hessianOutput = new HessianOutput(byteArrayOutputStream);
            hessianOutput.writeObject(obj);
            return byteArrayOutputStream.toByteArray();
        }catch (Exception e){
            throw new SerializeException("Hessian Serialization failed");
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try(ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes)){
            HessianInput hessianInput = new HessianInput(byteArrayInputStream);
            Object obj = hessianInput.readObject();
            return clazz.cast(obj);
        }catch (Exception e){
            throw new SerializeException("Hessian Deserialization failed");
        }
    }
}
