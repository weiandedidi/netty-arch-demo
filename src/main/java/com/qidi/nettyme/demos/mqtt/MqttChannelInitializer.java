package com.qidi.nettyme.demos.mqtt;

import com.qidi.nettyme.demos.websocket.HeartbeatHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-14 18:56
 */
public class MqttChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Autowired
    private MqttMessageHandler mqttMessageHandler;
    @Autowired
    private MqttHeartbeatHandler mqttHeartbeatHandler;
    @Autowired
    private EncryptHandler encryptHandler;
    @Value("${mqtt.keepalive.interval}")
    private int keepaliveInterval; // 探测心跳最大次数


    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // 设置读超时处理
        pipeline.addLast(new IdleStateHandler(0, 0, keepaliveInterval));
        // 加解密处理
        pipeline.addLast(encryptHandler);
        // MQTT解码器
        pipeline.addLast(new MqttDecoder(1024 * 8));
        // MQTT编码器
        pipeline.addLast(MqttEncoder.INSTANCE);
        // 心跳处理
        pipeline.addLast(mqttHeartbeatHandler);
        // 业务处理
        pipeline.addLast(mqttMessageHandler);
    }
}
