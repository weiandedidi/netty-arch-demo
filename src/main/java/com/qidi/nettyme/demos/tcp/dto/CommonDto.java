package com.qidi.nettyme.demos.tcp.dto;

import com.qidi.nettyme.demos.tcp.valueobject.RequestType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author qidi
 * @date 2019-04-24 18:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
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
        Header header = Header.buildSuccessServerRepsonseHeader(messageType);
        commonDto.setHeader(header);
        commonDto.setBody(body);
        return commonDto;
    }


}
