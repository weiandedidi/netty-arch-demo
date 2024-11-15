package com.qidi.nettyme.demos.mqtt;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-14 19:08
 */
@Component
@Slf4j
public class MqttHeartbeatHandler extends ChannelInboundHandlerAdapter {
    /**
     * 监听读空闲事件
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            //读写都空闲，记性操作
            if (event.state() == IdleState.ALL_IDLE) {
                log.info("No data received for 30 seconds, closing connection");
                //TODO  断开链接操作，连接池处理一下
                ctx.close();
            }
        }
        super.userEventTriggered(ctx, evt);
    }
}
