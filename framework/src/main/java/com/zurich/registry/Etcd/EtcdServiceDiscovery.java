package com.zurich.registry.Etcd;

import com.zurich.registry.ServiceRegistry;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KV;
import io.vertx.core.impl.ConcurrentHashSet;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2024/3/16 19:58
 * @description
 */
public class EtcdServiceDiscovery implements ServiceRegistry {

    public static final String ETCD_REGISTER_ROOT_PATH = "/my-rpc";

    private Client client;

    private KV kvClient;

    /**
     * 本机注册的节点 key 集合（用于维护续期）
     */
    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    /**
     * 注册中心服务缓存
     */

    /**
     * 正在监听的 key 集合
     */
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {

    }
}
