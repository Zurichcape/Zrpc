package com.zurich.registry.zk;

import com.zurich.registry.ServiceRegistry;
import com.zurich.utils.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/23 23:33
 * @description 基于zookeeper的服务注册
 */
public class ZkServiceRegistryImpl implements ServiceRegistry {
    @Override
    public void registerService(String rpcServiceName, InetSocketAddress inetSocketAddress) {
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName + inetSocketAddress.toString();
        CuratorFramework zkClient =CuratorUtils.getZkClient();
        CuratorUtils.createPersistentNode(zkClient,servicePath);
     }
}
