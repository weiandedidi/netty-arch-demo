package com.qidi.nettyme.demos.websocket;

import com.qidi.nettyme.demos.tcp.TcpChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 启动的时候打开server
 * @author maqidi
 * @version 1.0
 * @create 2024-11-13 15:03
 */
@Slf4j
//@Component
public class WebSocketServer {
    @Value("${websocket.port:8411}")
    private int websocketPort;
    @Value("${websocket.boss.threads:1}")
    private int bossThreadCount;
    @Value("${websocket.worker.threads:8}")
    private int workerThreadCount;
    /**
     * acceptor group
     */
    private NioEventLoopGroup bossGroup;
    /**
     * client group
     */
    private NioEventLoopGroup workerGroup;
    /**
     * 保存server的异步结果
     */
    ChannelFuture serverChannelFuture;
    @Autowired
    private WebChannelInitializer webChannelInitializer;

    @PostConstruct
    public ServerBootstrap webSocketServerBootstrap() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(bossThreadCount);
        workerGroup = new NioEventLoopGroup(workerThreadCount);
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true) //及时性高不延迟的，tcp配置，noDelay
                .childHandler(webChannelInitializer);
        try {
            serverChannelFuture = serverBootstrap.bind(websocketPort).sync();
            log.info("websocket server started on port: {}", websocketPort);
        } catch (InterruptedException e) {
            log.error("websocket server started error ", e);
            shutdown();
        }
        return serverBootstrap;
    }

    @PreDestroy
    public void shutdown() {
        try {
            //优雅关闭
            if (serverChannelFuture != null) {
                serverChannelFuture.channel().closeFuture().sync();
            }
        } catch (InterruptedException e) {
            log.error("websocket close serverChannelFuture InterruptedException e, ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("websocket server stopped ============");
        }
    }


}
