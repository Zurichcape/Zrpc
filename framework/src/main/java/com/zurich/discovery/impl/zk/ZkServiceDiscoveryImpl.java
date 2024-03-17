package com.zurich.discovery.impl.zk;

import com.zurich.enums.LoadBalanceEnum;
import com.zurich.enums.RpcErrorMessageEnum;
import com.zurich.exception.RpcException;
import com.zurich.extension.ExtensionLoader;
import com.zurich.loadbalance.LoadBalance;
import com.zurich.discovery.ServiceDiscovery;
import com.zurich.utils.CuratorUtils;
import com.zurich.remote.dto.RpcRequest;
import com.zurich.utils.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/23 23:33
 * @description 基于zookeeper的服务发现
 */
@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery {
    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl() {
        this.loadBalance = ExtensionLoader.getExtensionLoader(LoadBalance.class)
                .getExtension(LoadBalanceEnum.LOAD_BALANCE.getName());

    }

    @Override
    public InetSocketAddress findService(RpcRequest rpcRequest) {
        String rpcServiceName = rpcRequest.getRpcServiceName();
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient,rpcServiceName);
        if(CollectionUtil.isEmpty(serviceUrlList)){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND,rpcServiceName);
        }
        //负载均衡
        String targetServiceUrl = loadBalance.selectServiceAddress(serviceUrlList,rpcRequest);
        log.info("Successfully found the service address [{}]",targetServiceUrl);
        String[] socketAddressArray = targetServiceUrl.split(":");
        String host = socketAddressArray[0];
        int port = Integer.parseInt(socketAddressArray[1]);
        return new InetSocketAddress(host,port);
    }
}
