package com.qidi.nettyme.demos.mqtt.valueobject;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 存储通讯的通道信息
 * Created by RoyDeng on 18/2/4.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MqttLiveChannelInfo {
    /**
     * 唯一id
     */
    private String clientId;
    private Channel channel;
    private boolean authenticated;
    private long connectionTime;
    private String clientIp;
}
