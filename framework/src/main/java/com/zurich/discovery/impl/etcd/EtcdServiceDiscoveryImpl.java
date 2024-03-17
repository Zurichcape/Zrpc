package com.zurich.discovery.impl.etcd;

import com.zurich.discovery.ServiceDiscovery;
import com.zurich.enums.LoadBalanceEnum;
import com.zurich.extension.ExtensionLoader;
import com.zurich.loadbalance.LoadBalance;
import com.zurich.remote.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2024/3/16 20:29
 * @description
 */
public class EtcdServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public EtcdServiceDiscoveryImpl() {
        loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class).getExtension(LoadBalanceEnum.LOAD_BALANCE.getName());
    }

    @Override
    public InetSocketAddress findService(RpcRequest rpcRequest) {
        return null;
    }
}
