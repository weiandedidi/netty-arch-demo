package com.qidi.nettyme.demos.websocket;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-13 17:46
 */
@ChannelHandler.Sharable
@Component
@Slf4j
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    @Autowired
    SessionManager sessionManager;

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame frame) throws Exception {
        String message = frame.text();
        log.info("Received message: {}", message);
        //TODO 这里可以异步梳理问题

        // 处理业务逻辑，立即返回收到指令
        channelHandlerContext.channel().writeAndFlush(new TextWebSocketFrame("Server received: " + message));
    }

    /**
     * 建立连接
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("Client connected: {}", ctx.channel().remoteAddress());
        //这里连接的建立需要login操作后，保存session
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //客户端主动关闭，z
        log.info("Client disconnected: {}", ctx.channel().remoteAddress());
        sessionManager.removeSession(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("Channel exception", cause);
        sessionManager.removeSession(ctx.channel());
        super.exceptionCaught(ctx, cause);
    }
}
