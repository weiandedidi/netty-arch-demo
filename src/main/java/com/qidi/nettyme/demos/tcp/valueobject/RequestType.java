package com.qidi.nettyme.demos.tcp.valueobject;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-04 17:47
 */
public enum RequestType {
    REQUEST("request", "请求"),
    RESPONSE("response", "响应"),
    ;
    private String type;
    private String desc;

    RequestType(String type, String desc) {
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
