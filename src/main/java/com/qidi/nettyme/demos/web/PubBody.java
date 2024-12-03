package com.qidi.nettyme.demos.web;

import com.qidi.nettyme.demos.mqtt.dto.PublishBody;
import lombok.Data;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-26 20:37
 */
@Data
public class PubBody {
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


    /**
     * 构造结构体
     *
     * @param pubBody
     * @return
     */
    public static PublishBody convertToPublishBody(PubBody pubBody) {
        PublishBody publishBody = new PublishBody();
        publishBody.setTemperature(pubBody.getTemperature());
        publishBody.setHumidity(pubBody.getHumidity());
        publishBody.setPower(pubBody.getPower());
        publishBody.setStatus(pubBody.getStatus());
        publishBody.setX(pubBody.getX());
        publishBody.setY(pubBody.getY());
        return publishBody;
    }

}
