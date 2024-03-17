package com.zurich.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/22 11:29
 * @description
 */
@Slf4j
public final class PropertiesFileUtil {
    private PropertiesFileUtil(){}

    public static Properties readPropertiesFile(String fileName){
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        String rpcConfigPath = "";
        if(null != url){
            rpcConfigPath = url.getPath() + fileName;
        }
        Properties properties = null;
        try(InputStreamReader inputStreamReader = new InputStreamReader(
                new FileInputStream(rpcConfigPath), StandardCharsets.UTF_8
        )) {
            properties = new Properties();
            properties.load(inputStreamReader);
        }catch (IOException e){
            log.error("occur exception when read properties file [{}]",fileName);
        }
        return properties;
    }
}
