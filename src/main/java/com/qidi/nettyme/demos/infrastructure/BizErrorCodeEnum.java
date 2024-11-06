package com.qidi.nettyme.demos.infrastructure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-07-09 20:40
 */
public enum BizErrorCodeEnum implements ErrorCode {
    NETWORK_ERROR(501, "服务异常，请稍后重试"),
    SUCCESS(200, "success"),
    CONTEXT_INTERCEPT(204, "内容拦截"),

    NET_TIMEOUT(503, "服务超时"),
    //参数错误400
    //4 开头是业务错误码,
    PARAM_VALIDATION_ERROR(400001, "参数校验不通过"),
    METHOD_NOT_SUPPORTED(400002, "请求方式不支持"),
    //参数校验不通过,

    //5 开头为服务的错误码,
    OTHER_NETWORK_ERROR(500002, "调用三方服务网络异常"),
    RESIZE_IMAGE_ERROR(500003, "图片格式调整时异常"),
    DOWNLOAD_FILE_ERROR(500004, "对象存储下载文件失败"),
    IO_FILE_ERROR(500005, "本地磁盘IO异常"),

    ;

    /**
     * 错误码
     */
    private int code;
    /**
     * 错误描述
     */
    private String description;


    //维护一个静态内部map，存储code和BizErrorCodeEnum的映射
    private static final Map<Integer, BizErrorCodeEnum> code2Map = new HashMap<>();

    // 在一个独立的静态初始化块中填充 code2Map
    static {
        for (BizErrorCodeEnum value : BizErrorCodeEnum.values()) {
            code2Map.put(value.getCode(), value);
        }
    }

    /**
     * 根据编码查询枚举。
     *
     * @param code 编码。
     * @return 枚举。
     */
    public static BizErrorCodeEnum getByCode(Integer code) {
        return Optional.ofNullable(code2Map.get(code)).orElse(BizErrorCodeEnum.NETWORK_ERROR);
    }

    BizErrorCodeEnum(int code, String description) {
        this.code = code;
        this.description = description;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }
}
