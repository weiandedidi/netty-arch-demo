package com.qidi.nettyme.demos.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-04 16:03
 */
@Component
public class TcpChannelInitializer extends ChannelInitializer<SocketChannel> {

    @Value("${tcp.message.length:40960}")
    private int length;
    @Value("${tcp.keepalive.idle:60}")
    private int keepaliveIdle; // 空闲时间
    @Value("${tcp.keepalive.interval:20}")
    private int keepaliveInterval; // 探测间隔
    @Value("${tcp.keepalive.count:3}")
    private int keepaliveCount; // 探测次数

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        //心跳检测
        pipeline.addLast("idle", new IdleStateHandler(keepaliveInterval, 0, keepaliveIdle, TimeUnit.SECONDS));
        //        创建ssl单向验证服务
//        SSLEngine engine = factory.getServerContext(path).createSSLEngine();
//        engine.setUseClientMode(false);      //设置为服务器模式
//        pipeline.addLast("ssl", new SslHandler(engine));
        //自定义分隔符 拆包不能单例
        String delimiter = "$$";
        ByteBuf byteBuf = Unpooled.copiedBuffer(delimiter, CharsetUtil.UTF_8);
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(length, byteBuf));
        pipeline.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
        //消息处理 如果不是全局的统计，不能使用单例的，因为客户端断了重连会爆 is not a @Sharable handler错误
        pipeline.addLast("handler", new TcpDispatcherHandler());
    }
}
