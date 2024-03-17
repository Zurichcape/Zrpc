package com.zurich.remote.transport.netty.client;

import java.net.InetSocketAddress;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version 1.0
 * @Author zurich
 * @Date 2023/11/27 22:15
 * @description 存取 Channel对象
 */
@Slf4j
public class ChannelProvider {
    private final Map<String, Channel> channelMap;

    public ChannelProvider(){
        channelMap = new ConcurrentHashMap<>();
    }

    public Channel get(InetSocketAddress inetSocketAddress){
        String key = inetSocketAddress.toString();
        if(channelMap.containsKey(key)){
            Channel channel = channelMap.get(key);
            if(channel != null && channel.isActive()){
                return channel;
            }else{
                channelMap.remove(key);
            }
        }
        return null;
    }
    public void set(InetSocketAddress inetSocketAddress, Channel channel){
        String key = inetSocketAddress.toString();
        channelMap.put(key,channel);
    }

    public void remove(InetSocketAddress inetSocketAddress){
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        log.info("Channel map size:[{}]",channelMap.size());
    }
}
