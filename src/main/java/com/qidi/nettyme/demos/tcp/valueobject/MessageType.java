package com.qidi.nettyme.demos.tcp.valueobject;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-04 17:18
 */
public enum MessageType {
    //警告类消息
    //注册消息
    //登录消息
    LOGIN("LOGIN", "登录"),
    //心跳消息
    HEARTBEAT("HEARTBEAT", "心跳"),
    //消息
    MESSAGE("MESSAGE", "消息"),
    ;
    private String type;
    private String desc;

    private static final Map<String, MessageType> map = Maps.newHashMap();

    static {
        for (MessageType messageType : MessageType.values()) {
            map.put(messageType.getType(), messageType);
        }
    }

    public static MessageType getByType(String type) {
        return map.get(type);
    }

    MessageType(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }
}
