package com.qidi.nettyme.demos.websocket;

import com.qidi.nettyme.demos.tcp.valueobject.LiveChannelCache;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-13 17:08
 */
@Slf4j
public class HeartbeatHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    SessionManager sessionManager;
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        //websocket的处理
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
                    sessionManager.removeSession(ctx.channel());
            }
            //全空闲的时候关闭
        } else {
            //其他事件 放过
            super.userEventTriggered(ctx, evt);
        }
    }


}
