package com.qidi.nettyme.demos.web;

import com.qidi.nettyme.demos.mqtt.dto.PublishBody;
import lombok.Data;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

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

    @Mapper
    public interface PublishBodyCovert {
        PublishBodyCovert INSTANCE = Mappers.getMapper(PublishBodyCovert.class);

        PublishBody covertToPublishBody(PubBody pubBody);
    }
}
