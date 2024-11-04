package com.qidi.nettyme.demos.tcp.dto;

import lombok.Data;

/**
 * @author qidi
 * @date 2019-04-24 18:02
 */
@Data
public class Header {
    /**
     * 报文的唯一标识
     */
    private String requestId;
    private String version;
    private String serviceName;
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

}
