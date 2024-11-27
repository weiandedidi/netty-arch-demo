package com.qidi.nettyme.demos.mqtt.sender;

import com.google.common.cache.Cache;
import com.qidi.nettyme.demos.mqtt.dto.Body;
import com.qidi.nettyme.demos.mqtt.dto.CommonDto;
import com.qidi.nettyme.demos.mqtt.dto.PublishBody;
import com.qidi.nettyme.demos.mqtt.valueobject.MqttLiveChannelCache;
import com.qidi.nettyme.demos.mqtt.valueobject.TopicConstant;
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

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-26 19:25
 */
@Service
@Slf4j
public class MqttSendServiceImpl implements MqttSendService {

    final Cache<String, MqttLiveChannelCache> mqttClientCache;

    public MqttSendServiceImpl(@Qualifier("mqttClientCache") Cache<String, MqttLiveChannelCache> mqttClientCache) {
        this.mqttClientCache = mqttClientCache;
    }

    @Override
    public void sendMessage(String clientId, CommonDto<PublishBody> commonDto) {
        //根据clientId找到对应的channel，然后发送消息
        MqttLiveChannelCache mqttLiveChannelCache = mqttClientCache.getIfPresent(clientId);
        if (Objects.isNull(mqttLiveChannelCache) || !mqttLiveChannelCache.getChannel().isActive()) {
            log.error("通讯通道已断开,clientId {}", clientId);
            return;
        }
        String message = GsonUtil.toJsonString(commonDto);
        Channel channel = mqttLiveChannelCache.getChannel();
        //发送publish消息，topic是xxx，以后可以根据不同的topic写不同的类型
        //设置主题（topic）和 QoS（服务质量）等级
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        //设置topic， packetId后面可能需要斟酌
        MqttPublishVariableHeader variableHeader = new MqttPublishVariableHeader(TopicConstant.AGV_CMD_TOPIC, 0);
        //message转为ByteBuf payload
        ByteBuf payload = Unpooled.copiedBuffer(message, CharsetUtil.UTF_8);
        MqttPublishMessage publishMessage = new MqttPublishMessage(fixedHeader, variableHeader, payload);
        channel.writeAndFlush(publishMessage);
        log.info("publish success topic {}, message {}", TopicConstant.AGV_CMD_TOPIC, payload);
    }
}
