package com.qidi.nettyme.demos.mqtt.valueobject;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-04 17:18
 */
public enum MqttMsgType {
    //警告类消息
    WARNING("WARNING", "警告"),
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

    private static final Map<String, MqttMsgType> map = Maps.newHashMap();

    static {
        for (MqttMsgType mqttMsgType : MqttMsgType.values()) {
            map.put(mqttMsgType.getType(), mqttMsgType);
        }
    }

    public static MqttMsgType getByType(String type) {
        return map.get(type);
    }

    MqttMsgType(String type, String desc) {
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
