package com.qidi.nettyme.demos.mqtt.receiver;

import com.qidi.nettyme.demos.mqtt.dto.Body;
import com.qidi.nettyme.demos.mqtt.dto.CommonDto;
import com.qidi.nettyme.demos.mqtt.dto.PublishBody;
import io.netty.channel.ChannelHandlerContext;

/**
 * MQTT的接受信息的服务
 * 返回信息
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-11-26 17:50
 */
public interface MqttReceiverDemoService {
    /**
     * 这个接口是接收到消息的回调接口
     *
     * @param channelHandlerContext
     * @return
     */
    void handlePublish(ChannelHandlerContext channelHandlerContext, CommonDto<PublishBody> inputDto);
}
