package com.qidi.nettyme.demos.tcp.reciver;

import com.google.common.cache.Cache;
import com.qidi.nettyme.demos.tcp.dto.*;
import com.qidi.nettyme.demos.tcp.valueobject.LiveChannelCache;
import com.qidi.nettyme.demos.tcp.valueobject.MessageType;
import com.qidi.nettyme.demos.util.GsonUtil;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * @author qidi
 * @date 2019-04-25 11:00
 */
@Service
public class ReceiverDemoServiceImpl implements ReceiverDemoService {
    /**
     * clientId2ChannelCache
     */
    Cache<String, LiveChannelCache> tcpClientCache;
    /**
     * channelId2ChannelCache
     */
    Cache<String, LiveChannelCache> tcpChannelCache;

    public ReceiverDemoServiceImpl(@Qualifier("tcpClientCache") Cache<String, LiveChannelCache> tcpClientCache, @Qualifier("tcpChannelCache") Cache<String, LiveChannelCache> tcpChannelCache) {
        this.tcpClientCache = tcpClientCache;
        this.tcpChannelCache = tcpChannelCache;
    }

    @Override
    public CommonDto<Body> parseMessage(ChannelHandlerContext channelHandlerContext, String message) {
        CommonDto dto = GsonUtil.fromJsonString(message, CommonDto.class);
        //根据header做具体的分发，message
        Header requestHeader = dto.getHeader();
        MessageType type = MessageType.getByType(requestHeader.getMessageType());
        //需要特殊处理request的body
        Body body = new CommonBody(200, "success");
        switch (type) {
            case LOGIN:
                //长连接的建立登录消息，进行数据库中的设备 or 逻辑校验 checkValid
                body = new LoginBody("7766", "airConditioning");
                //TODO 登录成功，建立通讯通道
                //解析request的body，然后建立通讯通道
                //java读取服务器自身的ip地址，这里先用127.0.0.1,设置LiveChannelCache中的ScheduledFuture scheduledFuture
                registerChannelCache(channelHandlerContext, requestHeader);
                break;
            case HEARTBEAT:
                //心跳返回心跳消息，处理心跳的逻辑，返回心跳的报文
                body = new CommonBody(200, "心跳");
                break;
            case MESSAGE:
                //消息的其他自定义消息，处理自定义的消息，返回结果，底层异步处理，request的body处理
                //TODO 通讯通道校验，通过login后，每次请求的校验，防止没注册直接通讯，使用token存在远程缓存中。
                body = new CommonBody(200, "正常的消息");
                break;
            default:
                body = new CommonBody(400, "传递参数信息错误");
                break;
        }
        //返回json话的信息
        return CommonDto.buildSuccessServerResponseDto(requestHeader.getMessageType(), body);
    }

    /**
     * 注册通道
     *
     * @param channelHandlerContext
     * @param requestHeader
     */
    private void registerChannelCache(ChannelHandlerContext channelHandlerContext, Header requestHeader) {
        LiveChannelCache channelCache = new LiveChannelCache();
        channelCache.setClientId(requestHeader.getClientId());
        channelCache.setChannel(channelHandlerContext.channel());
        channelCache.setAuthenticated(true);
        channelCache.setConnectionTime(System.currentTimeMillis());
        channelCache.setClientIp(channelHandlerContext.channel().remoteAddress().toString());
        tcpClientCache.put(requestHeader.getClientId(), channelCache);
        tcpChannelCache.put(channelHandlerContext.channel().id().asLongText(), channelCache);
        //TODO redis远程注册 通讯通道的映射
    }

}
