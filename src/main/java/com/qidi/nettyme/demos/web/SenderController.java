package com.qidi.nettyme.demos.web;

import com.qidi.nettyme.demos.mqtt.dto.PublishBody;
import com.qidi.nettyme.demos.mqtt.sender.MqttSendService;
import com.qidi.nettyme.demos.tcp.dto.Body;
import com.qidi.nettyme.demos.tcp.dto.CommonDto;
import com.qidi.nettyme.demos.tcp.dto.Header;
import com.qidi.nettyme.demos.tcp.dto.LoginBody;
import com.qidi.nettyme.demos.tcp.sender.TcpSenderService;
import com.qidi.nettyme.demos.util.GsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-05 16:47
 */
@RestController
@RequestMapping("/sender")
public class SenderController {
    @Autowired
    TcpSenderService tcpSenderService;
    @Autowired
    MqttSendService mqttSendService;

    @RequestMapping(value = "/send", method = RequestMethod.POST)
    public ResponseVO<String> sendTcp(@RequestBody Header header) {
        LoginBody loginBody = new LoginBody("9999", "哈哈哈");
        CommonDto<Body> commonDto = new CommonDto<>(header, loginBody);
        tcpSenderService.sendMessage(header.getClientId(), commonDto);
        return ResponseVO.successEmptyResponse();
    }


    @PostMapping("/send/{clientId}")
    public ResponseVO<String> sendMqtt(@PathVariable String clientId, @RequestBody PubBody pubBody) {
        // 向特定 clientId 的客户端发送消息
        com.qidi.nettyme.demos.mqtt.dto.CommonDto<PublishBody> commonDto = new com.qidi.nettyme.demos.mqtt.dto.CommonDto<>();
        String headerStr = "{\"messageId\":\"1234567890\",\"version\":\"1.0\",\"clientId\":\"client123\",\"messageType\":\"DATA\",\"requestType\":\"request\",\"timestamp\":1634567890123,\"traceId\":\"trace123456\",\"interfaceName\":\"getTemperatureData\",\"code\":200}";
        com.qidi.nettyme.demos.mqtt.dto.Header header = GsonUtil.fromJsonString(headerStr, com.qidi.nettyme.demos.mqtt.dto.Header.class);
        commonDto.setHeader(header);
        PublishBody publishBody = PubBody.PublishBodyCovert.INSTANCE.covertToPublishBody(pubBody);
        commonDto.setBody(publishBody);
        mqttSendService.sendMessage(clientId, commonDto);
        return ResponseVO.successEmptyResponse();
    }
}
