package com.zurich.loadbalance.impl;

import com.zurich.loadbalance.AbstractLoadBalance;
import com.zurich.remote.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/23 23:39
 * @description
 */
@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    private final ConcurrentHashMap<String,ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest) {
        int identifyHashCode = System.identityHashCode(serviceUrlList);
        String rpcServiceName = rpcRequest.getRpcServiceName();
        ConsistentHashSelector selector = selectors.get(rpcServiceName);
        if(null == selector || selector.identifyHashCode != identifyHashCode){
            selectors.put(rpcServiceName,new ConsistentHashSelector(serviceUrlList,160,identifyHashCode));
            selector = selectors.get(rpcServiceName);
        }
        return selector.select(rpcServiceName+ Arrays.stream(rpcRequest.getParameters()));
    }

    static class ConsistentHashSelector{
        /**
         * 一致性哈希环，存放虚拟节点
         */
        private final TreeMap<Long, String> virtualInvokers;
        private final int identifyHashCode;

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identifyHashCode){
            this.virtualInvokers = new TreeMap<>();
            this.identifyHashCode = identifyHashCode;
            for(String invoker:invokers){
                for(int i=0;i<replicaNumber/4;i++){
                    byte[] digest = md5(invoker+i);
                    for(int h=0;h<4;h++){
                        long m = hash(digest,h);
                        virtualInvokers.put(m,invoker);
                    }
                }
            }
        }
        static byte[] md5(String key){
            MessageDigest md;
            try{
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            }catch (NoSuchAlgorithmException e){
                throw new IllegalStateException(e.getMessage(),e);
            }
            return md.digest();
        }
        static long hash(byte[] digest, int idx){
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }
        public String select(String rpcServiceKey){
            byte[] digest = md5(rpcServiceKey);
            return selectForKey(hash(digest,0));
        }
        public String selectForKey(long hashCode){
            Map.Entry<Long,String> entry = virtualInvokers.tailMap(hashCode,true).firstEntry();
            if(null == entry){
                entry = virtualInvokers.firstEntry();
            }
            return entry.getValue();
        }
    }
}
