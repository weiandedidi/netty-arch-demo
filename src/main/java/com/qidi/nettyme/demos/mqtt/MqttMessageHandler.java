package com.qidi.nettyme.demos.mqtt;

import com.google.common.cache.Cache;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;
import com.qidi.nettyme.demos.mqtt.dto.CommonDto;
import com.qidi.nettyme.demos.mqtt.dto.PublishBody;
import com.qidi.nettyme.demos.mqtt.receiver.MqttReceiverDemoService;
import com.qidi.nettyme.demos.mqtt.valueobject.MqttLiveChannelInfo;
import com.qidi.nettyme.demos.mqtt.valueobject.SubscriberInfo;
import com.qidi.nettyme.demos.util.GsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.*;

/**
 * 详见：https://blog.csdn.net/zwjzone/article/details/130089677   的配置
 * 代码源自claude  https://claude.ai/chat/ed2092f9-90cb-420a-a747-2ddd789af873
 * <p>
 * 消息类型	方向	描述
 * CONNECT	客户端 → 服务器	客户端请求连接到服务器。
 * CONNACK	服务器 → 客户端	服务器确认连接建立结果。
 * PUBLISH	客户端 ↔ 服务器	发布主题消息。
 * PUBACK	客户端 ↔ 服务器	确认 QoS 1 的 PUBLISH 消息。
 * PUBREC	客户端 ↔ 服务器	确认收到 QoS 2 的 PUBLISH 消息。
 * PUBREL	客户端 ↔ 服务器	确认发布的第二阶段（QoS 2）。
 * PUBCOMP	客户端 ↔ 服务器	确认发布的完成（QoS 2）。
 * SUBSCRIBE	客户端 → 服务器	请求订阅一个或多个主题。
 * SUBACK	服务器 → 客户端	确认订阅请求的结果。
 * UNSUBSCRIBE	客户端 → 服务器	请求取消订阅一个或多个主题。
 * UNSUBACK	服务器 → 客户端	确认取消订阅请求的结果。
 * PINGREQ	客户端 → 服务器	发送心跳请求，维持连接。
 * PINGRESP	服务器 → 客户端	响应心跳请求，保持连接。
 * DISCONNECT	客户端 ↔ 服务器	请求断开连接。
 * AUTH	客户端 ↔ 服务器	扩展认证功能，仅 MQTT 5.0 支持。
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-11-14 18:54
 */
@Component
@Slf4j
@ChannelHandler.Sharable
public class MqttMessageHandler extends SimpleChannelInboundHandler<MqttMessage> {
    final MqttReceiverDemoService mqttReceiverDemoService;
    //客户端id和通道映射
    final Cache<String, MqttLiveChannelInfo> mqttClientCache;
    //通道id和通道映射
    final Cache<String, MqttLiveChannelInfo> mqttChannelCache;
    //topic和订阅者映射
    final Cache<String, Map<String, SubscriberInfo>> mqttSubscriberCache;

    public MqttMessageHandler(MqttReceiverDemoService mqttReceiverDemoService, @Qualifier("mqttClientCache") Cache<String, MqttLiveChannelInfo> mqttClientCache, @Qualifier("mqttChannelCache") Cache<String, MqttLiveChannelInfo> mqttChannelCache, @Qualifier("mqttSubscriberCache") Cache<String, Map<String, SubscriberInfo>> mqttSubscriberCache) {
        this.mqttReceiverDemoService = mqttReceiverDemoService;
        this.mqttClientCache = mqttClientCache;
        this.mqttChannelCache = mqttChannelCache;
        this.mqttSubscriberCache = mqttSubscriberCache;
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, MqttMessage mqttMessage) throws Exception {
        switch (mqttMessage.fixedHeader().messageType()) {
            case CONNECT:
                //	在一个网络连接上，客户端只能发送一次CONNECT报文。服务端必须将客户端发送的第二个CONNECT报文当作协议违规处理并断开客户端的连接
                //	建议connect消息单独处理，用来对客户端进行认证管理等 这里直接返回一个CONNACK消息
                handleConnect(ctx, (MqttConnectMessage) mqttMessage);
                break;
            case PUBLISH:
                //发送消息
                handlePublish(ctx, (MqttPublishMessage) mqttMessage);
                break;
            case SUBSCRIBE:
                //订阅成功，用于客户端请求订阅一个或多个主题
                handleSubscribe(ctx, (MqttSubscribeMessage) mqttMessage);
                break;
            case UNSUBSCRIBE:
                //取消订阅，取消了一个主题而已
                handleUnSubscribe(ctx, (MqttUnsubscribeMessage) mqttMessage);
                break;
            case PINGREQ:
                handleHeartBeat(ctx);
                break;
            case DISCONNECT:
                //断开连接，断开连接后，服务端不再给客户端发送任何消息，客户端也不再给服务端发送任何消息。
                // 服务端收到客户端的DISCONNECT报文后，会断开与客户端的TCP连接。
                // 客户端收到服务端的DISCONNECT报文后，会断开与服务端的TCP连接。
                // 客户端在断开连接后，会发送一个MQTT_DISCONNECT消息，服务端收到该消息后，会断开与客户端的TCP连接。
                // 客户端在断开连接后，会发送一个MQTT
                handleDisconnect(ctx);
            case AUTH:
                //认证消息，用于扩展认证机制（如 SASL 等）
                break;
            default:
                //	其他消息类型，不处理
                break;
        }
        return;
    }

    /**
     * 处理断开连接
     *
     * @param channelHandlerContext
     */
    private void handleDisconnect(ChannelHandlerContext channelHandlerContext) {
        closeConnection(channelHandlerContext);
    }


    /**
     * 服务端收到客户端的MqttMessageType.CONNECT消息后，返回一个确认链接的消息MqttMessageType.CONNACK
     * 客户端CONNECT -> SERVER CONNACK 响应回去 （MQTT3.5.1）
     *
     * @param ctx
     * @param msg
     */
    private void handleConnect(ChannelHandlerContext ctx, MqttConnectMessage msg) {
        //如果已经建立连接了，就不在处理这个connect了
        String clientId = msg.payload().clientIdentifier();
//        if (Objects.nonNull(mqttClientCache.getIfPresent(clientId))) {
//            //建立已创建不合理
//            log.info("clientId:{} already connected", clientId);
//            return;
//        }
        log.info("clientId:{} connected", clientId);
        registerChannelCache(ctx, msg);
        // 处理连接请求
        MqttConnAckMessage acceptAck = new MqttConnAckMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, false)
        );
        ctx.writeAndFlush(acceptAck);
    }

    /**
     * 注册通讯通道，上游业务可以发送具体指令到服务器
     *
     * @param ctx
     * @param msg
     */
    private void registerChannelCache(ChannelHandlerContext ctx, MqttConnectMessage msg) {
        //建立连接
        String clientId = msg.payload().clientIdentifier();
        MqttLiveChannelInfo channelCache = new MqttLiveChannelInfo();
        channelCache.setClientId(clientId);
        channelCache.setChannel(ctx.channel());
        channelCache.setAuthenticated(true);
        channelCache.setConnectionTime(System.currentTimeMillis());
        channelCache.setClientIp(ctx.channel().remoteAddress().toString());
        mqttClientCache.put(clientId, channelCache);
        mqttChannelCache.put(ctx.channel().id().asLongText(), channelCache);
        //todo redis做远程注册，或者其他分布式注册中心，用于远程找到设备连接
    }

    /**
     * 处理推送过来的消息
     * <p>
     * 发消息的一端 发送消息头 PUBLISH  -> 接消息的一方，发布确认 PUBACK 用于 QoS 1 消息
     * PUBREC (5) 发布接收，用于 QoS 2 消息发布流程中的第一步
     * PUBREL (6) 发布释放，用于 QoS 2 消息发布流程中的第二步
     * PUBCOMP (7) 发布完成，用于 QoS 2 消息发布流程的最后一步
     *
     * @param ctx
     * @param msg
     */
    private void handlePublish(ChannelHandlerContext ctx, MqttPublishMessage msg) {
        MqttQoS qos = msg.fixedHeader().qosLevel();
        //如果有特殊的处qos指令处理方式进行处理

        // 处理客户端的发布消息，解析后序列化程指定对象，用于大类的区别，而payload用于具体的内容分发
        String topic = msg.variableHeader().topicName();
        //加密的消息也可以这里解密
        ByteBuf payload = msg.payload();
        log.info("Received publish topic {}, message {}", topic, msg.payload());
        // 将 ByteBuf 转换为字节数组
        byte[] payloadBytes = new byte[payload.readableBytes()];
        msg.payload().getBytes(0, payloadBytes);
        // 2. 将 payload 转换为字符串
        String data = new String(payloadBytes, CharsetUtil.UTF_8);
        //类型转换
        Type type = new TypeToken<CommonDto<PublishBody>>() {
        }.getType();
        CommonDto<PublishBody> inputDto = GsonUtil.fromJsonString(data, type);
        mqttReceiverDemoService.handlePublish(ctx, inputDto);

        //设置主题（topic）和 QoS（服务质量）等级
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.PUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
        //需要
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(msg.variableHeader().packetId());
        MqttPubAckMessage acceptAck = new MqttPubAckMessage(fixedHeader, variableHeader);
        ctx.writeAndFlush(acceptAck);
    }

    /**
     * SUBSCRIBE (8),订阅请求，用于客户端请求订阅一个或多个主题
     * SUBACK (9) 订阅确认，由服务器响应 SUBSCRIBE 报文
     *
     * @param ctx
     * @param msg
     */
    private void handleSubscribe(ChannelHandlerContext ctx, MqttSubscribeMessage msg) {
        // 处理订阅请求
        List<MqttTopicSubscription> topicSubscriptionList = msg.payload().topicSubscriptions();
        log.info("Received subscribe message, id : {}", msg.variableHeader().messageId());
        List<Integer> subQosList = subscribeTopicCache(ctx, topicSubscriptionList);
        // 发送 SUBACK 响应
        MqttFixedHeader subAckFixedHeader = new MqttFixedHeader(
                MqttMessageType.SUBACK,
                false,
                MqttQoS.AT_MOST_ONCE, //必须为这个值
                false,
                0
        );

        MqttSubAckPayload subAckPayload = new MqttSubAckPayload(subQosList);

        MqttSubAckMessage subAckMessage = new MqttSubAckMessage(
                subAckFixedHeader,
                MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                subAckPayload
        );
        //发送订阅完成
        try {
            ctx.writeAndFlush(subAckMessage);
        } catch (Exception e) {
            log.error("Failed to send SUBACK message", e);
        }
    }

    private List<Integer> subscribeTopicCache(ChannelHandlerContext ctx, List<MqttTopicSubscription> topicSubscriptionList) {
        //每个主题订阅的结果，严格要求队列顺序
        List<Integer> subQosList = Lists.newArrayList();
        MqttLiveChannelInfo mqttLiveChannelInfo = mqttChannelCache.getIfPresent(ctx.channel().id().asLongText());
        if (mqttLiveChannelInfo == null) {
            subQosList.add(MqttQoS.FAILURE.value());
            return subQosList;
        }

        topicSubscriptionList.forEach(topicSubscription -> {
            String topic = topicSubscription.topicName();
            //保存订阅者信息，从MqttSubscribeMessage获取订阅者的clientIdentifier，因为clientId只会在connect 时才存在，从缓存获取
            SubscriberInfo subscriberInfo = new SubscriberInfo(ctx.channel(), mqttLiveChannelInfo.getClientId());
            Map<String, SubscriberInfo> clientId2subInfoMap = Optional.ofNullable(mqttSubscriberCache.getIfPresent(topic)).orElseGet(Maps::newConcurrentMap);
            clientId2subInfoMap.put(mqttLiveChannelInfo.getClientId(), subscriberInfo);
            mqttSubscriberCache.put(topic, clientId2subInfoMap);
            MqttQoS subQos = checkAndHandleQoS(topicSubscription.qualityOfService());
            subQosList.add(subQos.value());
            log.info("Client {} subscribed to topic {} with QoS {}", mqttLiveChannelInfo.getClientId(), topic, subQosList);
        });
        return subQosList;
    }

    /**
     * 根据请求的 QoS 和 Broker 的实际处理情况来确定最终的 QoS
     * 后面这个主题可以修改
     */
    private MqttQoS checkAndHandleQoS(MqttQoS requestedQoS) {
        // 假设这里是一个示例，真实的 Broker 可以根据某些条件（比如订阅的主题）来调整 QoS 级别
        if (requestedQoS == MqttQoS.AT_MOST_ONCE) {
            // 如果请求的 QoS 是 AT_MOST_ONCE，则直接使用该 QoS
            return MqttQoS.AT_MOST_ONCE;
        } else if (requestedQoS == MqttQoS.AT_LEAST_ONCE) {
            // 如果请求的 QoS 是 AT_LEAST_ONCE，Broker 可能决定使用更高的 QoS
            // 这里可以添加对 QoS 1 的特殊处理逻辑
            return MqttQoS.AT_LEAST_ONCE;
        } else if (requestedQoS == MqttQoS.EXACTLY_ONCE) {
            // 如果请求的 QoS 是 EXACTLY_ONCE，则可能会有一些限制
            // 比如某些主题不允许 QoS 2 级别的传递
            return MqttQoS.EXACTLY_ONCE;
        }

        // 默认情况下，返回最简单的 QoS
        return MqttQoS.AT_MOST_ONCE;
    }


    /**
     * UNSUBSCRIBE (10) 取消订阅请求，用于客户端取消订阅一个或多个主题。
     * UNSUBACK (11) 取消订阅确认，由服务器响应 UNSUBSCRIBE 报文。
     *
     * @param ctx
     * @param msg
     */
    private void handleUnSubscribe(ChannelHandlerContext ctx, MqttUnsubscribeMessage msg) {
        // 处理取消订阅请求
        // 获取消息ID
        int messageId = msg.variableHeader().messageId();
        log.info("Received unsubscribe messageId: {}, payload {}", messageId, msg.payload());
        //待取消的标题
        List<String> topics = msg.payload().topics();
        topics.forEach(topic -> unsubscribeTopic(topic, ctx));
        // 发送 UNSUBACK 响应
        MqttFixedHeader unsubAckFixedHeader = new MqttFixedHeader(
                MqttMessageType.UNSUBACK,
                false,
                MqttQoS.AT_MOST_ONCE,
                false,
                0
        );

        MqttUnsubAckMessage unsubAckMessage = new MqttUnsubAckMessage(
                unsubAckFixedHeader,
                MqttMessageIdVariableHeader.from(messageId)
        );
        ctx.writeAndFlush(unsubAckMessage);
    }

    /**
     * 移除订阅者
     *
     * @param topic
     * @param ctx
     * @return
     */
    private boolean unsubscribeTopic(String topic, ChannelHandlerContext ctx) {
        try {
            Map<String, SubscriberInfo> clientId2subInfoMap = mqttSubscriberCache.getIfPresent(topic);

            if (clientId2subInfoMap == null) {
                return false;
            }

            // 移除订阅者，通过channel获取clientId,从而移除订阅者
            MqttLiveChannelInfo mqttLiveChannelInfo = mqttChannelCache.getIfPresent(ctx.channel().id().asLongText());
            if (mqttLiveChannelInfo == null) {
                return false;
            }
            String clientId = mqttLiveChannelInfo.getClientId();
            SubscriberInfo removeResult = clientId2subInfoMap.remove(clientId);

            // 如果主题没有订阅者，则删除主题
            if (clientId2subInfoMap.isEmpty()) {
                mqttSubscriberCache.invalidate(topic);
            }
            log.info("Client {} unsubscribed from topic {}", clientId, topic);
            return true;
        } catch (Exception e) {
            log.error("Error unsubscribing from topic {}", topic, e);
            return false;
        }
    }

    /**
     * PINGREQ (12) 心跳请求，用于客户端向服务器发送心跳包以维持连接。
     * PINGRESP (13) 心跳响应，用于服务器对 PINGREQ 报文的响应。
     *
     * @param channelHandlerContext
     */
    private void handleHeartBeat(ChannelHandlerContext channelHandlerContext) {
        //
        // 处理客户端心跳请求，返回心跳响应
        MqttMessage heartbeatResponse = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PINGRESP, false, MqttQoS.AT_MOST_ONCE, false, 0),
                null,
                null
        );
        channelHandlerContext.writeAndFlush(heartbeatResponse);
        log.info("Received heartbeat message");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            //读写都空闲，记性操作
            if (event.state() == IdleState.ALL_IDLE) {
                log.info("No data received for 30 seconds, closing connection");
                //TODO  断开链接操作，连接池处理一下
//                closeConnection(ctx);
            }
        }
        super.userEventTriggered(ctx, evt);
    }

    /**
     * 关闭连接
     *
     * @param ctx
     */
    private void closeConnection(ChannelHandlerContext ctx) {
        MqttLiveChannelInfo mqttLiveChannelInfo = mqttChannelCache.getIfPresent(ctx.channel().id().asLongText());
        if (mqttLiveChannelInfo != null) {
            mqttChannelCache.invalidate(mqttLiveChannelInfo.getChannel().id().asLongText());
            mqttClientCache.invalidate(mqttLiveChannelInfo.getClientId());
        }
        ctx.close();
    }


}
