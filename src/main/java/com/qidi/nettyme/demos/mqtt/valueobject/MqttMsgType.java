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
    //心跳消息
    HEARTBEAT("HEARTBEAT", "心跳"),
    //消息
    MESSAGE("MESSAGE", "消息"),
    //report，客户端发送report消息的时候，需要转发给 智慧大屏服务端（如果是大厂的话，会订阅这个消息，大厂应该自己内部不处理这样的逻辑，这里用户测试业务逻辑编写的）
    REPORT("REPORT", "消息"),
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
