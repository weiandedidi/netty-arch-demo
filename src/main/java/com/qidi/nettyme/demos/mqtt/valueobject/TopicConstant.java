package com.qidi.nettyme.demos.mqtt.valueobject;

import lombok.extern.slf4j.Slf4j;

/**
 * 这个是topic的常量类
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-11-27 11:21
 */
public class TopicConstant {
    //小车状态上报topic
    public static final String AGV_REPORT_TOPIC = "agv/report";
    //小车接受指令的topic
    public static final String AGV_CMD_TOPIC = "agv/cmd";
    //室内的智慧大屏，用于显示agv的信息和空调等信息，空调等客户端都发到这个topic
    public static final String SCREEN_TOPIC = "screen/show";
}
