package com.qidi.nettyme.demos.websocket;


import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-13 17:48
 */
@Slf4j
@Component
public class SessionManager {
    private static ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    public static final AttributeKey<String> SESSION_ID = AttributeKey.valueOf("sessionId");
    /**
     * 创建的时间
     */
    public static final AttributeKey<LocalDateTime> CONNECT_TIME = AttributeKey.valueOf("connectTime");
    /**
     * 最大的连接数量控制
     */
    @Value("${websocket.max-connections:10000}")
    private int maxConnections;

    public Optional<WebSocketSession> addSession(Channel channel) {
        if (sessions.size() >= maxConnections) {
            log.warn("Max connections limit reached: {}", maxConnections);
            return Optional.empty();
        }

        String sessionId = generateSessionId();
        WebSocketSession session = new WebSocketSession(sessionId, channel);
        //这里set了sessionId，所以channel里面有session所以在读空闲的时候,可以使用channel的这个sessionId属性获取session
        channel.attr(SESSION_ID).set(sessionId);
        channel.attr(CONNECT_TIME).set(LocalDateTime.now());

        sessions.put(sessionId, session);
        log.info("Session created: {}", sessionId);
        return Optional.of(session);
    }

    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    public WebSocketSession getSession(Channel channel) {
        String sessionId = channel.attr(SESSION_ID).get();
        return sessions.get(sessionId);
    }

    public void removeSession(String sessionId) {
        WebSocketSession session = sessions.remove(sessionId);
        //移除后，做一些通知什么的
    }

    public void removeSession(Channel channel) {
        String sessionId = channel.attr(SESSION_ID).get();
        if (sessions.containsKey(sessionId)) {
            WebSocketSession session = sessions.remove(sessionId);
        }
        //移除后，做一些通知什么的
    }


    /**
     * 会话的id
     *
     * @return
     */
    private static String generateSessionId() {
        return java.util.UUID.randomUUID().toString();
    }
}
