package com.qidi.nettyme.demos.mqtt;

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
public class MqttClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        // SSL处理
//        pipeline.addLast(SslHandlerProvider.getServerSslContext());
        // MQTT解码器
        pipeline.addLast(new MqttDecoder(1024 * 8));
        // MQTT编码器
        pipeline.addLast(MqttEncoder.INSTANCE);
    }
}
