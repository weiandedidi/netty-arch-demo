package com.qidi.nettyme.demos.mqtt;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.qidi.nettyme.demos.mqtt.valueobject.MqttLiveChannelCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * tcp的长连接服务器配置类，使用netty的SO_KEEPALIVE进行心跳检测
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-11-04 14:49
 */
@Configuration
@Slf4j
public class MqttConfig {
    //TODO 这里分布式的应用可以使用redis作为远程连接池的实现，本地内存记住通讯通道，远程redis通过clientId找到服务器，分布式转发到那台服务器，然后找到通道发消息。
    //通讯的通道的本地内存保存，最多2w个通讯, key是clientId
    @Bean(name = "mqttClientCache")
    public Cache<String, MqttLiveChannelCache> mqttClientCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(20000)
                .build();
    }

    //通讯的通道的本地内存保存，最多2w个通讯, key是ChannelId，关闭通讯时使用
    @Bean(name = "mqttChannelCache")
    public Cache<String, MqttLiveChannelCache> mqttChannelCache() {
        return CacheBuilder.newBuilder()
                .maximumSize(20000)
                .build();
    }


}
