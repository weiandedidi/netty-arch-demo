package com.qidi.nettyme.demos.tcp.sender;

import com.google.common.cache.Cache;
import com.qidi.nettyme.demos.tcp.dto.Body;
import com.qidi.nettyme.demos.tcp.dto.CommonDto;
import com.qidi.nettyme.demos.tcp.valueobject.LiveChannelCache;
import com.qidi.nettyme.demos.util.GsonUtil;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-05 16:26
 */
@Service
@Slf4j
public class SenderServiceImpl implements SenderService {

    @Value("${tcp.retry.count:3}")
    private int maxRetries;

    final Cache<String, LiveChannelCache> tcpClientCache;

    public SenderServiceImpl(@Qualifier("tcpClientCache") Cache<String, LiveChannelCache> tcpClientCache) {
        this.tcpClientCache = tcpClientCache;
    }

    @Override
    public void sendMessage(String clientId, CommonDto<Body> commonDto) {
        //TODO 根据clientId找到对应的channel，然后发送消息
        LiveChannelCache channelCache = tcpClientCache.getIfPresent(clientId);
        if (Objects.isNull(channelCache) || !channelCache.getChannel().isActive()) {
            log.error("通讯通道已断开,clientId {}", clientId);
            return;
        }
        String message = GsonUtil.toJsonString(commonDto);
        sendMessageWithRetry(channelCache, message);
    }

    /**
     * 指定测试重试
     *
     * @param message
     */
    private void sendMessageWithRetry(LiveChannelCache channelCache, String message) {
        int retryCount = 1;
        while (retryCount < maxRetries) {
            try {
                Channel channel = channelCache.getChannel();
                channel.writeAndFlush(message + "\r\n" + "$$").sync();
                return;
            } catch (Exception e) {
                log.error("Failed to send message, attempt {}. Error: {}", retryCount + 1, e.getMessage());
                if (++retryCount >= 3) {
                    log.error("Max retries reached. Could not send message.");
                }
            }
        }
    }
}
