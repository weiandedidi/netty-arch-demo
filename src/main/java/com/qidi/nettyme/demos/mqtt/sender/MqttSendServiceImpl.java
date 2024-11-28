package com.qidi.nettyme.demos.mqtt.sender;

import com.google.common.cache.Cache;
import com.qidi.nettyme.demos.mqtt.dto.CommonDto;
import com.qidi.nettyme.demos.mqtt.dto.PublishBody;
import com.qidi.nettyme.demos.mqtt.valueobject.MqttLiveChannelInfo;
import com.qidi.nettyme.demos.util.GsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Random;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-26 19:25
 */
@Service
@Slf4j
public class MqttSendServiceImpl implements MqttSendService {

    final Cache<String, MqttLiveChannelInfo> mqttClientCache;

    public MqttSendServiceImpl(@Qualifier("mqttClientCache") Cache<String, MqttLiveChannelInfo> mqttClientCache) {
        this.mqttClientCache = mqttClientCache;
    }

    @Override
    public void sendMessage(String clientId, CommonDto<PublishBody> commonDto, String topic) {
        //根据clientId找到对应的channel，然后发送消息
        MqttLiveChannelInfo mqttLiveChannelInfo = mqttClientCache.getIfPresent(clientId);
        if (Objects.isNull(mqttLiveChannelInfo) || !mqttLiveChannelInfo.getChannel().isActive()) {
            log.error("通讯通道已断开,clientId {}", clientId);
            return;
        }
        String message = GsonUtil.toJsonString(commonDto);
        Channel channel = mqttLiveChannelInfo.getChannel();
        //发送publish消息，topic是xxx，以后可以根据不同的topic写不同的类型
        //设置主题（topic）和 QoS（服务质量）等级
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        //设置topic， packetId后面可能需要斟酌 ，packetId 一般是用户生成的，并且唯一，服务端只是使用，这里使用个随机数
        int packetId = Math.abs(new Random().nextInt(30)) * 10;
        MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(topic, packetId);
        //message转为ByteBuf payload
        ByteBuf payload = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
        MqttPublishMessage publishMessage = new MqttPublishMessage(fixedHeader, variableHeader, payload);
        channel.writeAndFlush(publishMessage);
        log.info("publish success clientId {}, topic {}, message {}", clientId, topic, GsonUtil.toJsonString(commonDto));
    }
}
