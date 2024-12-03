package com.qidi.nettyme.demos.mqtt.dto;

import com.qidi.nettyme.demos.tcp.valueobject.MessageType;
import com.qidi.nettyme.demos.tcp.valueobject.RequestType;
import lombok.Data;

import java.util.UUID;

/**
 * @author qidi
 * @date 2019-04-24 18:02
 */
@Data
public class Header {
    /**
     * 报文的唯一标识
     */
    private String messageId;
    private String version;
    private String clientId;
    /**
     * 协议的主题
     */
    private String topic;
    /**
     * 消息类型
     */
    private String messageType;
    /**
     * 请求还是返回  request  or response
     */
    private String requestType;
    /**
     * 时间戳
     */
    private Long timestamp;
    /**
     * traceId
     */
    private String traceId;
    /**
     * 报文的方法名
     */
    private String interfaceName;
    /**
     * 服务请求的状态码（非具体的业务请求，200请求没问题，但是系统内部具体的衣长需要处理）
     */
    private Integer code;
    //客户端的系统信息，版本号，时间，xxx

    public static Header buildSuccessServerRepsonseHeader(String messageType) {
        // 创建自定义 Header
        Header header = new Header();
        header.setMessageId("123456");
        header.setVersion("1.0");
        header.setRequestType(RequestType.RESPONSE.getType());
        header.setMessageType(messageType);
        header.setTimestamp(System.currentTimeMillis());
        header.setTraceId("TRACE123456");
        header.setInterfaceName("myMethod");
        header.setCode(200);
        return header;
    }

    public static Header buildHeartBeatRequestHeader() {
        // 创建自定义 Header
        Header header = new Header();
        header.setMessageId("123456");
        header.setVersion("1.0");
        header.setRequestType(RequestType.REQUEST.getType());
        header.setMessageType(MessageType.HEARTBEAT.getType());
        header.setTimestamp(System.currentTimeMillis());
        header.setTraceId(UUID.randomUUID().toString());
        header.setInterfaceName("myMethod");
        header.setCode(200);
        return header;
    }
}
