package com.qidi.nettyme.demos.mqtt;

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
 * 测试方法的客户端详见：com.qidi.nettyme.demos.mqtt.MqttClient
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-11-14 18:00
 */
@Component
@Slf4j
public class MqttServer {
    @Value("${mqtt.port}")
    private int port;

    @Value("${mqtt.boss.threads:1}")
    private int bossThreadCount;
    @Value("${mqtt.worker.threads:8}")
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
    private MqttChannelInitializer mqttChannelInitializer;


    @PostConstruct
    public ServerBootstrap start() throws Exception {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(bossThreadCount);
        workerGroup = new NioEventLoopGroup(workerThreadCount);
        bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                //ChannelOption.SO_BACKLOG对应的是tcp/ip协议listen函数中的backlog参数，
                // 函数listen(int socketfd,int backlog)用来初始化服务端可连接队列，
                // 服务端处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接，
                // 多个客户端来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理，backlog参数指定了队列的大小
                .option(ChannelOption.SO_BACKLOG, 1024)
                //快速复用,防止服务端重启端口被占用的情况发生
                .option(ChannelOption.SO_REUSEADDR, true)
                .childHandler(mqttChannelInitializer)
                //如果TCP_NODELAY没有设置为true,那么底层的TCP为了能减少交互次数,会将网络数据积累到一定的数量后,
                // 服务器端才发送出去,会造成一定的延迟。在互联网应用中,通常希望服务是低延迟的,建议将TCP_NODELAY设置为true
                .childOption(ChannelOption.TCP_NODELAY, true)
                //默认的心跳间隔是7200s即2小时。Netty默认关闭该功能。
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            serverChannelFuture = bootstrap.bind(port).sync();
            log.info("MQTT server started on port {}", port);
        } catch (Exception e) {
            log.error("Failed to start MQTT server", e);
            shutdown();
        }
        return bootstrap;
    }

    @PreDestroy
    public void shutdown() {
        if (serverChannelFuture != null) {
            try {
                serverChannelFuture.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                log.error("mqtt close serverChannelFuture InterruptedException e, ", e);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                log.info("mqtt server stopped ============");

            }
        }
    }
}
