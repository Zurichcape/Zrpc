package com.zurich.compress.gzip;

import com.zurich.compress.Compress;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/23 22:14
 * @description
 */
public class GzipCompress implements Compress {
    private static final int BUFFER_SIZE = 1024 * 9;

    @Override
    public byte[] compress(byte[] bytes){
        if(null == bytes){
            throw new NullPointerException("bytes is null");
        }
        try(ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out)){
            gzip.write(bytes);
            gzip.flush();
            gzip.finish();
            return out.toByteArray();
        }catch (IOException e){
            throw new RuntimeException("gzip compress error",e);
        }
    }

    @Override
    public byte[] decompress(byte[] bytes) {
        if(null == bytes){
            throw new NullPointerException("bytes is null");
        }
        try(ByteArrayOutputStream out= new ByteArrayOutputStream();
            GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(bytes))){
            byte[] buffer = new byte[BUFFER_SIZE];
            int n;
            while((n=gzipIn.read(buffer))>-1){
                out.write(buffer,0,n);
            }
            return out.toByteArray();
        }catch (IOException e){
            throw new RuntimeException("gzip decompress error",e);
        }
    }
}
