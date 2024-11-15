package com.qidi.nettyme.demos.mqtt;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 加解密处理
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-11-14 18:54
 */
@Component
@Slf4j
public class EncryptHandler extends ChannelDuplexHandler {
    @Autowired
    private SecurityUtil securityUtil;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            byte[] array = new byte[buf.readableBytes()];
            buf.readBytes(array);
            byte[] decrypted = securityUtil.decrypt(array);
            ctx.fireChannelRead(Unpooled.wrappedBuffer(decrypted));
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (msg instanceof ByteBuf) {
            ByteBuf buf = (ByteBuf) msg;
            byte[] array = new byte[buf.readableBytes()];
            buf.readBytes(array);
            byte[] encrypted = securityUtil.encrypt(array);
            ctx.write(Unpooled.wrappedBuffer(encrypted), promise);
        } else {
            ctx.write(msg, promise);
        }
    }
}
