package com.qidi.nettyme.demos.mqtt.receiver;

import com.google.common.cache.Cache;
import com.qidi.nettyme.demos.mqtt.dto.CommonDto;
import com.qidi.nettyme.demos.mqtt.dto.Header;
import com.qidi.nettyme.demos.mqtt.dto.PublishBody;
import com.qidi.nettyme.demos.mqtt.sender.MqttSendService;
import com.qidi.nettyme.demos.mqtt.valueobject.MqttMsgType;
import com.qidi.nettyme.demos.mqtt.valueobject.SubscriberInfo;
import com.qidi.nettyme.demos.mqtt.valueobject.TopicConstant;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-26 17:55
 */
@Service
@Slf4j
public class MqttReceiverDemoServiceImpl implements MqttReceiverDemoService {

    final Cache<String, Map<String, SubscriberInfo>> mqttSubscriberCache;
    final MqttSendService mqttSendService;

    public MqttReceiverDemoServiceImpl(@Qualifier("mqttSubscriberCache") Cache<String, Map<String, SubscriberInfo>> mqttSubscriberCache, MqttSendService mqttSendService) {
        this.mqttSubscriberCache = mqttSubscriberCache;
        this.mqttSendService = mqttSendService;
    }

    @Override
    public void handlePublish(ChannelHandlerContext channelHandlerContext, CommonDto<PublishBody> inputDto) {
        //具体的数据处理分发类
        Header requestHeader = inputDto.getHeader();
        MqttMsgType type = MqttMsgType.getByType(requestHeader.getMessageType());
        switch (type) {
            //因为connect方法中已经做了login这里就不用继续做了
            case MESSAGE:
                //这里写返回逻辑
                //例如上报具体的内容，之后可以有策略类分发内容
                log.info("接受mqtt消息，然后处理");
                break;
            case REPORT:
                //如果是上报类的消息，转发给智慧大屏，偏业务逻辑方向了，正常应该不是这么操作
                forwardMessageToScreenSubscribers(inputDto);
                break;
            default:
                break;
            //返回结果处理
        }
    }

    /**
     * 纯业务代码，其实没必要再broker中实现（server）转发到智慧大屏
     *
     * @param inputDto
     */
    private void forwardMessageToScreenSubscribers(CommonDto<PublishBody> inputDto) {
        //找到大屏的topic，转发到这个topic
        String topic = TopicConstant.SCREEN_TOPIC;
        Map<String, SubscriberInfo> subscriberInfoMap = mqttSubscriberCache.getIfPresent(topic);
        if (subscriberInfoMap == null) {
            return;
        }
        //转发给所有的订阅者
        for (SubscriberInfo subscriberInfo : subscriberInfoMap.values()) {
            //转发消息，这里可以做策略，例如根据clientId做路由转发，或者根据消息类型做路由转发
            mqttSendService.sendMessage(subscriberInfo.getClientId(), inputDto, topic);
        }
    }

}
