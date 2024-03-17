package com.zurich.extension;

import com.zurich.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/21 19:04
 * @description
 */
@Slf4j
public final class ExtensionLoader<T> {
    private static final String SERVICE_DIRECTORY = "META-INF/extensions/";
    /**
     * 插件加载器
     */
    private static final Map<Class<?>,ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    /**
     *
     */
    private static final Map<Class<?>,Object> EXTENSION_INSTANCE = new ConcurrentHashMap<>();

    private final Class<?> type;
    /**
     * 缓存插件实例
     */
    private final Map<String, Holder<Object>> cachedInstances = new ConcurrentHashMap<>();
    /**
     * 缓存插件类
     */
    private final Holder<Map<String, Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type) {
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    public static<S> ExtensionLoader<S> getExtensionLoader(Class<S> type){
        if(null == type){
            throw new IllegalArgumentException("Extension type should not be null");
        }
        if(!type.isInterface()){
            throw new IllegalArgumentException("Extension type must be interface");
        }
        if(type.getAnnotation(SPI.class) == null){
            throw new IllegalArgumentException("Extension type must be annotated by @SPI");
        }
        //首先尝试从缓存中加载，如果未命中，则创新一个新的插件
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>)EXTENSION_LOADERS.get(type);
        if(null == extensionLoader){
            EXTENSION_LOADERS.putIfAbsent(type,new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    @SuppressWarnings("unchecked")
    public T getExtension(String name){
        if(StringUtil.isBlank(name)){
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        //首先尝试从缓存中获取，如果未命中，新建一个
        Holder<Object> holder = cachedInstances.get(name);
        if(null == holder){
            cachedInstances.putIfAbsent(name,new Holder<>());
            holder = cachedInstances.get(name);
        }
        //如果实例不存在则创建一个单例
        Object instance = holder.get();
        if(null == instance){
            synchronized (holder){
                instance = holder.get();
                if(null == instance){
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        return(T)instance;
    }

    @SuppressWarnings("unchecked")
    private T createExtension(String name){
        //从文件中加载所有类型为T的插件类，并且根据name获取
        Class<?> clazz = getExtensionClasses().get(name);
        if(null == clazz){
            throw new RuntimeException("no such extension of name: "+ name);
        }
        T instance = (T)EXTENSION_INSTANCE.get(clazz);
        if(null == instance){
            try{
                EXTENSION_INSTANCE.putIfAbsent(clazz,clazz.newInstance());
                instance =(T)EXTENSION_INSTANCE.get(clazz);
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
        return instance;
    }

    private Map<String,Class<?>> getExtensionClasses(){
        //从缓存中加载插件类，双检锁
        Map<String,Class<?>> classes = cachedClasses.get();
        if(null == classes){
            synchronized (cachedClasses){
                classes = cachedClasses.get();
                if(null == classes){
                    classes = new HashMap<>();
                    //从本地的插件目录中加载所有插件
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    private void loadDirectory(Map<String,Class<?>> extensionClasses){
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();
        try{
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if(null != urls){
                while (urls.hasMoreElements()){
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses,classLoader,resourceUrl);
                }
            }
        }catch (IOException e){
            log.error(e.getMessage());
         }
    }

    /**
     * 读取配置文件中的插件类
     * @param extensionClasses
     * @param classLoader
     * @param resourceUrl
     */
    private void loadResource(Map<String,Class<?>> extensionClasses,ClassLoader classLoader,URL resourceUrl){
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), StandardCharsets.UTF_8))){
            String line;
            while((line = reader.readLine()) != null){
                final int ci = line.indexOf('#');
                if(ci > 0){
                    //#后的都是注释直接忽略
                    line = line.substring(0,ci);
                }
                line = line.trim();
                if(line.length()>0){
                    try {
                        final int ei = line.indexOf('=');
                        String name = line.substring(0,ei).trim();
                        String clazzName = line.substring(ei+1).trim();
                        if(name.length()>0 && clazzName.length()>0){
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name,clazz);
                        }
                    }catch (ClassNotFoundException e){
                        log.error(e.getMessage());
                    }
                }
            }
        }catch (IOException e){
            log.error(e.getMessage());
        }
    }
}
