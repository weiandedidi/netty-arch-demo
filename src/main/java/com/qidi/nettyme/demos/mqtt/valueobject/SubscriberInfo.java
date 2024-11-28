package com.qidi.nettyme.demos.mqtt.valueobject;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * 因为订阅者和topic 是多对多的关系，所以里面不能存储topic的信息
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-11-28 15:32
 */
@Data
@NoArgsConstructor
public class SubscriberInfo {
    /**
     * 客户端的唯一id
     */
    private String clientId;
    /**
     * 订阅topic的客户端的通讯通道
     */
    private Channel channel;
    /**
     * 订阅的时间
     */
    private long subscribeTimestamp;

    /**
     * 通过通讯通道和客户端id构造一个订阅者信息
     *
     * @param channel
     * @param clientId
     */
    public SubscriberInfo(Channel channel, String clientId) {
        this.channel = channel;
        this.clientId = clientId;
        this.subscribeTimestamp = System.currentTimeMillis();
    }

    //重写hashcode和equals，clientId相同就是同一个
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriberInfo that = (SubscriberInfo) o;
        return Objects.equals(clientId, that.clientId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId);
    }
}
