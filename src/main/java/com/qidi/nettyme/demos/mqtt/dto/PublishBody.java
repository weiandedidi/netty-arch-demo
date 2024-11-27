package com.qidi.nettyme.demos.mqtt.dto;

import com.google.gson.reflect.TypeToken;
import com.qidi.nettyme.demos.util.GsonUtil;
import lombok.Data;

import java.lang.reflect.Type;

/**
 * 模拟agv小车的（空调等）
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-11-26 17:30
 */
@Data
public class PublishBody implements Body {
    //温度
    private Double temperature;
    //湿度
    private Double humidity;
    //电量
    private Integer power;
    //状态
    private Integer status;
    //横坐标
    private Integer x;
    //纵坐标
    private Integer y;

//    public static void main(String[] args) {
//        String json = "{\"header\":{\"temperature\":25.5,\"humidity\":60,\"power\":85,\"status\":1,\"x\":10,\"y\":20},\"body\":{\"temperature\":25.5,\"humidity\":60,\"power\":85,\"status\":1,\"x\":10,\"y\":20}}";
//        Type type = new TypeToken<CommonDto<PublishBody>>() {}.getType();
//        CommonDto<PublishBody> commonDto = GsonUtil.fromJsonString(json, type);
//        System.out.println(commonDto.getBody().getTemperature());
//    }

}
