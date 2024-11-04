package com.qidi.nettyme.demos.tcp;

import com.qidi.nettyme.demos.tcp.dto.Body;
import com.qidi.nettyme.demos.tcp.dto.CommonDto;
import com.qidi.nettyme.demos.tcp.reciver.ReceiverDemoService;
import com.qidi.nettyme.demos.util.GsonUtil;
import com.qidi.nettyme.demos.util.SpringUtils;
import com.qidi.nettyme.demos.util.TraceIdUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-04 16:19
 */
@Component
@Slf4j
public class TcpDispatcherHandler extends ChannelInboundHandlerAdapter {
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
        CommonDto<Body> response = receiverDemoService.parseMessage(str);
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
        super.channelInactive(ctx);
    }


}
