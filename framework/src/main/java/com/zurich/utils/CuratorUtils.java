package com.zurich.utils;

import com.zurich.enums.RpcConfigEnum;
import com.zurich.utils.PropertiesFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/23 23:34
 * @description
 */
@Slf4j
public class CuratorUtils {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final Set<String> REGISTER_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2081";

    public CuratorUtils(){}

    /**
     * 创建持久节点，和临时节点不同，当客户端连接断开后该节点也不会被删除
     * @param zkClient 客户端
     * @param path 节点路径
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path){
        try{
            if(REGISTER_PATH_SET.contains(path) || zkClient.checkExists().forPath(path) != null){
                log.error("The node already exists. The node is:[{}]",path);
            }else{
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
            }
            REGISTER_PATH_SET.add(path);
        }catch (Exception e){
            log.error("create persistent node for the path [{}] fail",path);
        }
    }

    /**
     *
     * @param zkClient 客户端
     * @param rpcServiceName rpc服务端名称
     * @return 获得某个节点下所有子节点
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName){
        if(SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try{
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName,result);
            registerWatcher(rpcServiceName,zkClient);
        }catch (Exception e){
            log.error("get children nodes for path [{}] fail",servicePath);
        }
        return result;
    }

    /**
     * 注册监听某个节点的变化
     * @param rpcServiceName rpc服务端名称
     * @param zkClient 客户端
     */
    public static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient,servicePath,true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent)->{
            List<String> serviceAddress = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName,serviceAddress);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }

    /**
     * 清空该server上的所有注册服务
     * @param zkClient 客户端
     * @param inetSocketAddress 当前server地址
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        REGISTER_PATH_SET.stream().parallel().forEach(p->{
            try {
                if(p.endsWith(inetSocketAddress.toString())){
                    zkClient.delete().forPath(p);
                }
            }catch (Exception e){
                log.error("clear registry for path [{}] fail",p);
            }
        });
        log.info("All registered service on the server are cleared: [{}]",REGISTER_PATH_SET.toString());
    }

    /**
     * 返回或创建一个zookeeper客户端
     * @return 返回一个zookeeper客户端
     */
    public static CuratorFramework getZkClient(){
        //检查用户是否设置了zk地址
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress = properties != null
                && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) != null
                ? properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue())
                : DEFAULT_ZOOKEEPER_ADDRESS;

        //如果zookeeper客户端已经启动了,直接返回实例
        if(zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED){
            return zkClient;
        }
        //重试策略。重试3次，每次重试都会增加睡眠时间
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME,MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            //阻塞30s等待连接，否则抛出错误
            if(!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)){
                throw new RuntimeException("Time out waiting to connect to zookeeper");
            }
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return zkClient;
    }
}
