package com.qidi.nettyme.demos.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * 启动的时候打开 注解
 * @author maqidi
 * @version 1.0
 * @create 2024-11-06 09:55
 */
//@Component
@Slf4j
public class TcpServer {
    @Value("${tcp.port}")
    private int tcpPort;
    @Value("${tcp.boss.threads:2}")
    private int bossThreadCount;
    @Value("${tcp.worker.threads:8}")
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
    TcpChannelInitializer tcpChannelInitializer;

    @PostConstruct
    public ServerBootstrap tcpServerBootstrap() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(bossThreadCount);
        workerGroup = new NioEventLoopGroup(workerThreadCount);
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true) //及时性高不延迟的，tcp配置，noDelay
                .childOption(ChannelOption.SO_RCVBUF, 32 * 1024)
                .childOption(ChannelOption.SO_SNDBUF, 32 * 1024)
                .childHandler(tcpChannelInitializer);
        try {
            serverChannelFuture = serverBootstrap.bind(tcpPort).sync();
            log.info("tcp server started on port: {}", tcpPort);
        } catch (InterruptedException e) {
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
            log.error("serverChannelFuture InterruptedException e, ", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
