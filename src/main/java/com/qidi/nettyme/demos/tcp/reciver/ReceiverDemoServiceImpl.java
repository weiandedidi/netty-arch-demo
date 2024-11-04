package com.qidi.nettyme.demos.tcp.reciver;

import com.qidi.nettyme.demos.tcp.dto.*;
import com.qidi.nettyme.demos.tcp.valueobject.MessageType;
import com.qidi.nettyme.demos.util.GsonUtil;
import org.springframework.stereotype.Component;

/**
 * @author qidi
 * @date 2019-04-25 11:00
 */
@Component
public class ReceiverDemoServiceImpl implements ReceiverDemoService {
    @Override
    public CommonDto<Body> parseMessage(String message) {
        CommonDto dto = GsonUtil.fromJsonString(message, CommonDto.class);
        //根据header做具体的分发，message
        Header requestHeader = dto.getHeader();
        MessageType type = MessageType.getByType(requestHeader.getMessageType());

        //需要特殊处理request的body
        Body body = new CommonBody(200, "success");
        switch (type) {
            case LOGIN:
                //长连接的建立登录消息，进行数据库中的设备 or 逻辑校验 checkValid
                body = new LoginBody("7766", "airConditioning");
                break;
            case HEARTBEAT:
                //心跳返回心跳消息，处理心跳的逻辑，返回心跳的报文
                body = new CommonBody(200, "心跳");
                break;
            case MESSAGE:
                //消息的其他自定义消息，处理自定义的消息，返回结果，底层异步处理，request的body处理
                body = new CommonBody(200, "正常的消息");
                break;
            default:
                body = new CommonBody(400, "传递参数信息错误");
                break;
        }
        //返回json话的信息
        return CommonDto.buildSuccessServerResponseDto(requestHeader.getMessageType(), body);
    }
}
