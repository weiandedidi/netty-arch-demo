package com.qidi.nettyme.demos.websocket;

import com.qidi.nettyme.demos.tcp.TcpDispatcherHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-13 16:59
 */
@Component
public class WebChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Value("${websocket.keepalive.idle:15}")
    private int keepaliveIdle; // 空闲时间
    @Value("${websocket.keepalive.interval:30}")
    private int keepaliveInterval; // 探测心跳最大次数
    @Value("${websocket.keepalive.count:3}")
    private int keepaliveCount; // 探测最大的容忍次数


    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new IdleStateHandler(keepaliveIdle, 0, keepaliveInterval));
        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));
        //自定义心跳检测处理方式，处理IdleStateEvent事件
        pipeline.addLast(new HeartbeatHandler());
        pipeline.addLast(new WebSocketHandler());

    }
}
