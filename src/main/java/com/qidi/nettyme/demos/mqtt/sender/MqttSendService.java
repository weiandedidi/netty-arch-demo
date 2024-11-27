package com.qidi.nettyme.demos.mqtt.sender;


import com.qidi.nettyme.demos.mqtt.dto.Body;
import com.qidi.nettyme.demos.mqtt.dto.CommonDto;
import com.qidi.nettyme.demos.mqtt.dto.PublishBody;

/**
 * MQTT的分发处理接口
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-11-26 19:25
 */
public interface MqttSendService {
    void sendMessage(String clientId, CommonDto<PublishBody> commonDto);

}
