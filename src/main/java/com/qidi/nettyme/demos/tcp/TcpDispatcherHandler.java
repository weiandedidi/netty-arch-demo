package com.qidi.nettyme.demos.tcp;

import com.google.common.cache.Cache;
import com.qidi.nettyme.demos.tcp.dto.Body;
import com.qidi.nettyme.demos.tcp.dto.CommonDto;
import com.qidi.nettyme.demos.tcp.dto.Header;
import com.qidi.nettyme.demos.tcp.reciver.ReceiverDemoService;
import com.qidi.nettyme.demos.tcp.valueobject.LiveChannelCache;
import com.qidi.nettyme.demos.util.GsonUtil;
import com.qidi.nettyme.demos.util.SpringUtils;
import com.qidi.nettyme.demos.util.TraceIdUtil;
import io.netty.channel.*;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 在多个ChannelPipeline中共享，需要确保它实现了@Sharable注解
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-11-04 16:19
 */
@Component
@Slf4j
@ChannelHandler.Sharable
public class TcpDispatcherHandler extends ChannelInboundHandlerAdapter {

    Cache<String, LiveChannelCache> tcpClientCache;
    Cache<String, LiveChannelCache> tcpChannelCache;

    public TcpDispatcherHandler(@Qualifier("tcpClientCache") Cache<String, LiveChannelCache> tcpClientCache, @Qualifier("tcpChannelCache") Cache<String, LiveChannelCache> tcpChannelCache) {
        this.tcpClientCache = tcpClientCache;
        this.tcpChannelCache = tcpChannelCache;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        try {
            if (!(cause instanceof IOException)) {
                log.error("TcpDispatcherHandler exception， e=", cause);
            }
            ctx.close();
        } finally {
            // 清理 ThreadLocal 变量
            TraceIdUtil.clear();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //解析具体的消息
        String receiveJson = (String) msg;
        receiveJson.trim();
        //生成traceId，并设置上下文，traceId可以从客户端读取
        String traceId = TraceIdUtil.generateTraceId();

        try {
            TraceIdUtil.setTraceId(traceId);
            //埋入traceId
            log.info("接到客户端消息 message={}", msg);
            //处理报文
            handlerData(ctx, msg);
        } finally {
            // 清理 ThreadLocal 变量
            TraceIdUtil.clear();
        }
    }

    /**
     * 处理消息的核心类
     *
     * @param channelHandlerContext
     * @param msg
     */
    private void handlerData(ChannelHandlerContext channelHandlerContext, Object msg) {
        //具体的报文处理方法 分发处理，然后进行相应操作

        //这里service处理方法必须使用spring的Context获取bean而不是注入，因为是new出来的对象，不是注入
        ReceiverDemoService receiverDemoService = SpringUtils.getBean(ReceiverDemoService.class);
        log.info("DispatcherHandler instance receiverDemoService ***********");
        String str = (String) msg;
        CommonDto<Body> response = receiverDemoService.parseMessage(channelHandlerContext, str);
        //回复报文
        Channel channel = channelHandlerContext.channel();
        //协定报文分隔符
        String data = GsonUtil.toJsonString(response);
        ChannelFuture channelFuture = channel.writeAndFlush(data + "\r\n" + "$$");
        try {
            channelFuture = channelFuture.sync();
        } catch (InterruptedException e) {
            log.info("发送报文中断了, e={}, casues={}", e.getMessage(), e.getCause());
        }
        //发送是否成功
        channelFuture.isSuccess();

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //断线事件上报，并且移除 通讯通道，对方主动断开
        log.error("断线事件上报 channelInactive");
        closeConnection(ctx);
        super.channelInactive(ctx);
    }

    /**
     * 监听用户的事件，如果是读空闲事件
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            //读超时的时候，发送心跳
            switch (event.state()) {
                case READER_IDLE:
                    // 客户端读空闲时发送心跳包, 服务端不用。
//                    sendHeartbeat(ctx);
//                    log.info("读超时，发送操作");
                    break;
                case WRITER_IDLE:
                    //不做操作
                    break;
                case ALL_IDLE:
                    // 60秒内没有读写操作，关闭连接
                    log.info("读写超时，关闭连接");
                    closeConnection(ctx);
            }
            //全空闲的时候关闭
        } else {
            //其他事件 放过
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 关闭连接
     *
     * @param ctx
     */
    private void closeConnection(ChannelHandlerContext ctx) {
        //移除通讯通道的缓存映射
        LiveChannelCache liveChannelCache = tcpChannelCache.getIfPresent(ctx.channel().id().asLongText());
        if (liveChannelCache == null) {
            return;
        }
        //移除链接池缓存
        tcpChannelCache.invalidate(liveChannelCache.getChannel().id().asLongText());
        tcpClientCache.invalidate(liveChannelCache.getClientId());
        //TODO 远程的redis移除 通讯通道映射

        //关闭连接,这里没有移除关闭
        ctx.close();
    }

    /**
     * 发送心跳包
     *
     * @param ctx
     */
    private void sendHeartbeat(ChannelHandlerContext ctx) {
        //发送心跳消息，心跳只有header
        Header header = Header.buildHeartBeatRequestHeader();
        CommonDto<Body> heartbeat = new CommonDto<>(header, null);
        Channel channel = ctx.channel();
        String data = GsonUtil.toJsonString(heartbeat);
        ChannelFuture channelFuture = channel.writeAndFlush(data + "\r\n" + "$$");
        try {
            channelFuture = channelFuture.sync();
        } catch (InterruptedException e) {
            log.info("发送心跳报文失效, e={}, casues={}", e.getMessage(), e.getCause());
        }
        //发送是否成功
        channelFuture.isSuccess();
    }
}
