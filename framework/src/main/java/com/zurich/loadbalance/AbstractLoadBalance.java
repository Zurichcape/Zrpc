package com.zurich.loadbalance;

import com.zurich.remote.dto.RpcRequest;
import com.zurich.utils.CollectionUtil;

import java.util.List;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/23 23:41
 * @description
 */
public abstract class AbstractLoadBalance implements LoadBalance{
    @Override
    public String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest) {
        if(CollectionUtil.isEmpty(serviceUrlList)){
            return null;
        }
        if(serviceUrlList.size() == 1){
            return serviceUrlList.get(0);
        }
        return doSelect(serviceUrlList,rpcRequest);
    }
    protected abstract String doSelect(List<String> serviceUrlList,RpcRequest rpcRequest);
}
