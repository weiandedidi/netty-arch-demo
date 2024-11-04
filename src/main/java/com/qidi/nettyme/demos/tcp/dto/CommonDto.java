package com.qidi.nettyme.demos.tcp.dto;

import com.qidi.nettyme.demos.tcp.valueobject.RequestType;
import lombok.Data;

/**
 * @author qidi
 * @date 2019-04-24 18:02
 */
@Data
public class CommonDto<T> {
    /**
     * 请求的消息头
     */
    Header header;
    /**
     * 请求的内容
     */
    T body;

    public static <T> CommonDto<T> buildSuccessServerResponseDto(String messageType, T body) {
        CommonDto<T> commonDto = new CommonDto<T>();
        Header header = buildSuccessServerRepsonseHeader(messageType);
        commonDto.setHeader(header);
        commonDto.setBody(body);
        return commonDto;
    }

    public static Header buildSuccessServerRepsonseHeader(String messageType) {
        // 创建自定义 Header
        Header header = new Header();
        header.setRequestId("123456");
        header.setVersion("1.0");
        header.setServiceName("MyService");
        header.setRequestType(RequestType.RESPONSE.getType());
        header.setMessageType(messageType);
        header.setTimestamp(System.currentTimeMillis());
        header.setTraceId("TRACE123456");
        header.setInterfaceName("myMethod");
        header.setCode(200);
        return header;
    }
}
