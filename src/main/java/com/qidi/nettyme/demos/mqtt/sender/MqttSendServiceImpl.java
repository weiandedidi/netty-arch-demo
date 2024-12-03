package com.qidi.nettyme.demos.mqtt.sender;

import com.google.common.cache.Cache;
import com.qidi.nettyme.demos.mqtt.dto.CommonDto;
import com.qidi.nettyme.demos.mqtt.dto.PublishBody;
import com.qidi.nettyme.demos.mqtt.valueobject.SubscriberInfo;
import com.qidi.nettyme.demos.util.GsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;
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

    Cache<String, Map<String, SubscriberInfo>> mqttSubscriberCache;

    public MqttSendServiceImpl(@Qualifier("mqttSubscriberCache") Cache<String, Map<String, SubscriberInfo>> mqttSubscriberCache) {
        this.mqttSubscriberCache = mqttSubscriberCache;
    }

    @Override
    public void sendMessage(String topic, CommonDto<PublishBody> commonDto) {
        //根据clientId找到对应的channel，然后发送消息
        Map<String, SubscriberInfo> subscriberInfoMap = mqttSubscriberCache.getIfPresent(topic);
        if (Objects.isNull(subscriberInfoMap)) {
            log.error("没有订阅者，topic {}", topic);
            return;
        }
        subscriberInfoMap.values().forEach(subscriberInfo -> sendToSubscriber(topic, commonDto, subscriberInfo));
    }

    private void sendToSubscriber(String topic, CommonDto<PublishBody> commonDto, SubscriberInfo subscriberInfo) {
        Channel channel = subscriberInfo.getChannel();

        if (Objects.isNull(channel) || !channel.isActive()) {
            log.error("通讯通道已断开,clientId {}", subscriberInfo.getClientId());
            return;
        }
        String message = GsonUtil.toJsonString(commonDto);
        //发送publish消息，topic是xxx，以后可以根据不同的topic写不同的类型
        //设置主题（topic）和 QoS（服务质量）等级
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBLISH, false, MqttQoS.AT_LEAST_ONCE, false, 0);
        //设置topic， packetId后面可能需要斟酌 ，packetId 一般是用户生成的，并且唯一，服务端只是使用，这里使用个随机数
        int packetId = Math.abs(new Random().nextInt(30)) * 10;
        MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(topic, packetId);
        //message转为ByteBuf payload
        ByteBuf payload = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
        MqttPublishMessage publishMessage = new MqttPublishMessage(fixedHeader, variableHeader, payload);
        channel.writeAndFlush(publishMessage);
        log.info("publish success clientId {}, topic {}, message {}", subscriberInfo.getClientId(), topic, GsonUtil.toJsonString(commonDto));
    }
}
