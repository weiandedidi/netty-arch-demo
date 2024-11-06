package com.qidi.nettyme.demos.web;

import com.qidi.nettyme.demos.infrastructure.BizErrorCodeEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * controller层的返回对象
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-07-11 10:46
 */
@Setter
@Getter
public class ResponseVO<T> {

    /**
     * 返回状态码
     */
    private Integer code;
    /**
     * 返回描述信息
     */
    private String msg;
    /**
     * 数据
     */
    private T data;

    public ResponseVO() {
    }

    public ResponseVO(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ResponseVO(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }


    /**
     * 业务成功返回业务代码,描述和返回的参数
     */
    public static <T> ResponseVO<T> successResponse(T data) {
        return new ResponseVO<>(BizErrorCodeEnum.SUCCESS.getCode(), BizErrorCodeEnum.SUCCESS.getDescription(), data);
    }

    /**
     * 业务成功返回业务代码,描述和返回的参数
     */
    public static <T> ResponseVO<T> successEmptyResponse() {
        return new ResponseVO<>(BizErrorCodeEnum.SUCCESS.getCode(), BizErrorCodeEnum.SUCCESS.getDescription());
    }

    /**
     * 业务异常返回业务代码和描述信息
     */
    public static <T> ResponseVO<T> failure(BizErrorCodeEnum bizErrorCodeEnum) {
        return new ResponseVO<>(bizErrorCodeEnum.getCode(), bizErrorCodeEnum.getDescription());
    }

    /**
     * 业务异常返回业务代码和描述信息，二级详细的错误内容
     */
    public static <T> ResponseVO<T> failureDetailMessage(BizErrorCodeEnum bizErrorCodeEnum, String detailMessage) {
        return new ResponseVO<>(bizErrorCodeEnum.getCode(), detailMessage);
    }

    /**
     * 业务异常返回业务代码和描述信息，二级详细的错误内容
     */
    public static <T> ResponseVO<T> failureDetailMessage(BizErrorCodeEnum bizErrorCodeEnum, String detailMessage, T data) {
        return new ResponseVO<>(bizErrorCodeEnum.getCode(), detailMessage, data);
    }

    /**
     * 业务异常返回业务代码和描述信息和数据
     */
    public static <T> ResponseVO<T> failureData(BizErrorCodeEnum bizErrorCodeEnum, T data) {
        return new ResponseVO<>(bizErrorCodeEnum.getCode(), bizErrorCodeEnum.getDescription(), data);
    }

}
