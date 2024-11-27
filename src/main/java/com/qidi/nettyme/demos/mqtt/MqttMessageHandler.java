package com.qidi.nettyme.demos.mqtt;

import com.google.common.cache.Cache;
import com.google.gson.reflect.TypeToken;
import com.qidi.nettyme.demos.mqtt.dto.CommonDto;
import com.qidi.nettyme.demos.mqtt.dto.PublishBody;
import com.qidi.nettyme.demos.mqtt.receiver.MqttReceiverDemoService;
import com.qidi.nettyme.demos.mqtt.valueobject.MqttLiveChannelCache;
import com.qidi.nettyme.demos.tcp.valueobject.LiveChannelCache;
import com.qidi.nettyme.demos.util.GsonUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

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
public class MqttMessageHandler extends SimpleChannelInboundHandler<MqttMessage> {
    final MqttReceiverDemoService mqttReceiverDemoService;
    final Cache<String, MqttLiveChannelCache> mqttClientCache;
    final Cache<String, MqttLiveChannelCache> mqttChannelCache;

    public MqttMessageHandler(MqttReceiverDemoService mqttReceiverDemoService, @Qualifier("mqttClientCache") Cache<String, MqttLiveChannelCache> mqttClientCache, @Qualifier("mqttChannelCache") Cache<String, MqttLiveChannelCache> mqttChannelCache) {
        this.mqttReceiverDemoService = mqttReceiverDemoService;
        this.mqttClientCache = mqttClientCache;
        this.mqttChannelCache = mqttChannelCache;
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
        MqttLiveChannelCache channelCache = new MqttLiveChannelCache();
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
        MqttMessageIdVariableHeader variableHeader = MqttMessageIdVariableHeader.from(msg.variableHeader().packetId());
        MqttPubAckMessage acceptAck = new MqttPubAckMessage(fixedHeader, variableHeader);
        ctx.writeAndFlush(acceptAck);
        log.info("Received publish topic {}, message {}", topic, msg.payload());
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
        log.info("Received subscribe message: {}", msg.payload());
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
        log.info("Received unsubscribe message: {}", msg.payload());
        //连接处理
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
                closeConnection(ctx);
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
        MqttLiveChannelCache mqttLiveChannelCache = mqttChannelCache.getIfPresent(ctx.channel().id().asLongText());
        if (mqttLiveChannelCache != null) {
            mqttChannelCache.invalidate(mqttLiveChannelCache.getChannel().id().asLongText());
            mqttClientCache.invalidate(mqttLiveChannelCache.getClientId());
        }
        ctx.close();
    }


}
