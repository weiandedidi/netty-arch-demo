package com.qidi.nettyme.demos.mqtt;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.mqtt.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 详见：https://blog.csdn.net/zwjzone/article/details/130089677   的配置
 * 代码源自claude  https://claude.ai/chat/ed2092f9-90cb-420a-a747-2ddd789af873
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-11-14 18:54
 */
@Component
@Slf4j
public class MqttMessageHandler extends SimpleChannelInboundHandler<MqttMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, MqttMessage mqttMessage) throws Exception {
        switch (mqttMessage.fixedHeader().messageType()) {
            case CONNECT:
                //	在一个网络连接上，客户端只能发送一次CONNECT报文。服务端必须将客户端发送的第二个CONNECT报文当作协议违规处理并断开客户端的连接
                //	建议connect消息单独处理，用来对客户端进行认证管理等 这里直接返回一个CONNACK消息
                handleConnect(channelHandlerContext, (MqttConnectMessage) mqttMessage);
                break;
            case PUBLISH:
                //发送消息
                handlePublish(channelHandlerContext, (MqttPublishMessage) mqttMessage);
                break;
            case SUBSCRIBE:
                //订阅成功
                handleSubscribe(channelHandlerContext, (MqttSubscribeMessage) mqttMessage);
                break;
            case UNSUBSCRIBE:
                //取消订阅，断开的时候
                handleUnSubscribe(channelHandlerContext, (MqttUnsubscribeMessage) mqttMessage);
                break;
        }
        return;
    }

    /**
     * 服务端收到客户端的MqttMessageType.CONNECT消息后，返回一个确认链接的消息MqttMessageType.CONNACK
     *
     * @param ctx
     * @param msg
     */
    private void handleConnect(ChannelHandlerContext ctx, MqttConnectMessage msg) {
        // 处理连接请求
        MqttConnAckMessage acceptAck = new MqttConnAckMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, false)
        );
        ctx.writeAndFlush(acceptAck);
    }

    /**
     * 处理推送过来的消息
     *
     * @param ctx
     * @param msg
     */
    private void handlePublish(ChannelHandlerContext ctx, MqttPublishMessage msg) {
        // 处理发布消息
        //设置主题（topic）和 QoS（服务质量）等级
        log.info("Received publish message: {}", msg.payload());
    }

    private void handleSubscribe(ChannelHandlerContext ctx, MqttSubscribeMessage msg) {
        // 处理订阅请求
        log.info("Received subscribe message: {}", msg.payload());
    }

    private void handleUnSubscribe(ChannelHandlerContext ctx, MqttUnsubscribeMessage msg) {
        // 处理取消订阅请求
        log.info("Received unsubscribe message: {}", msg.payload());
    }


}
