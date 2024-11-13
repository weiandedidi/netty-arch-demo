package com.qidi.nettyme.demos.websocket;

import io.netty.channel.Channel;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-13 17:58
 */
@Data
public class WebSocketSession {
    private final String sessionId;
    private final Channel channel;
    private final LocalDateTime connectTime;
    private String userId;  // 可选的用户标识
    /**
     * 通讯拓展的类，例如服务端的一些特殊信息，后面明确可以使用具体的类代替，而不是这个map
     */
    private Map<String, Object> attributes = new ConcurrentHashMap<>();

    public WebSocketSession(String sessionId, Channel channel) {
        this.sessionId = sessionId;
        this.channel = channel;
        this.connectTime = LocalDateTime.now();
    }
}
