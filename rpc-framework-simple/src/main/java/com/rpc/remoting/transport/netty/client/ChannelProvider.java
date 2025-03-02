package com.rpc.remoting.transport.netty.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理和维护客户端与服务器之间的网络连接通道（Channel）
 */
@Slf4j
public class ChannelProvider {
    // 存储和缓存这些连接通道，确保客户端能够高效地复用已有的连接，同时提供对连接通道的增删查操作
    private final Map<String, Channel> channelMap;

    public ChannelProvider() {
        channelMap = new ConcurrentHashMap<>();
    }

    public Channel get(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        // 确保传入的地址是否有通道连接
        if (channelMap.containsKey(key)) {
            Channel channel = channelMap.get(key);
            // 确保连接是否活跃，活跃则直接复用该通道，避免重复连接
            if (channel != null && channel.isActive()) {
                return channel;
            } else {
                channelMap.remove(key);
            }
        }
        return null;
    }

    public void set(InetSocketAddress inetSocketAddress, Channel channel) {
        String key = inetSocketAddress.toString();
        channelMap.put(key, channel);
    }

    public void remove(InetSocketAddress inetSocketAddress) {
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        log.info("Channel map size: [{}]", channelMap.size());
    }
}
