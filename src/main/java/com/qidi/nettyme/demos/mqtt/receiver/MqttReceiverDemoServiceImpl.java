package com.qidi.nettyme.demos.mqtt.receiver;

import com.qidi.nettyme.demos.mqtt.dto.*;
import com.qidi.nettyme.demos.mqtt.valueobject.MqttMsgType;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-26 17:55
 */
@Service
@Slf4j
public class MqttReceiverDemoServiceImpl implements MqttReceiverDemoService {

    @Override
    public void handlePublish(ChannelHandlerContext channelHandlerContext, CommonDto<PublishBody> inputDto) {
        //具体的数据处理分发类
        Header requestHeader = inputDto.getHeader();
        MqttMsgType type = MqttMsgType.getByType(requestHeader.getMessageType());
        switch (type) {
            case LOGIN:
                //长连接的建立登录消息，进行数据库中的设备 or 逻辑校验 checkValid
                break;
            case MESSAGE:
                //这里写返回逻辑
                //例如上报具体的内容，之后可以有策略类分发内容
                log.info("接受mqtt消息，然后处理");
                break;
            default:
                break;
            //返回结果处理
        }
    }

}
