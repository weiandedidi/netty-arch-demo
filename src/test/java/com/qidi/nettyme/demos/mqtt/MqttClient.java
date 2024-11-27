package com.qidi.nettyme.demos.mqtt;

import com.qidi.nettyme.demos.mqtt.dto.CommonBody;
import com.qidi.nettyme.demos.mqtt.dto.CommonDto;
import com.qidi.nettyme.demos.mqtt.dto.Header;
import com.qidi.nettyme.demos.mqtt.valueobject.TopicConstant;
import com.qidi.nettyme.demos.util.GsonUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.mqtt.*;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-26 19:16
 */
public class MqttClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8412;
    public static final String CLIENT_ID = "client123";

    public void connect() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new MqttClientInitializer());

            Channel channel = bootstrap.connect(SERVER_HOST, SERVER_PORT).sync().channel();

            // 发送MQTT连接消息
            MqttConnectMessage connectMessage = createConnectMessage();
            channel.writeAndFlush(connectMessage);

//            // 发布消息示例
//            MqttPublishMessage publishMessage = createPublishMessage();
//            channel.writeAndFlush(publishMessage);

            channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
    public static void main(String[] args) throws Exception {
        MqttClient client = new MqttClient();
        client.connect();

    }

    private MqttConnectMessage createConnectMessage() {
        // 创建连接消息，生成MqttConnectMessage
        // 创建固定头部
        MqttFixedHeader fixedHeader = new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_MOST_ONCE, false, 0);

        // 创建可变头部
        MqttConnectVariableHeader variableHeader = new MqttConnectVariableHeader(
                "MQTT", // 协议名称
                4,      // 协议版本
                false,  // 用户名
                false,   // 密码
                false,   // will标志，如果设置为 true，则表示 Will 消息将被保留，即使没有订阅者也会被存储
                MqttQoS.AT_MOST_ONCE.value(), // 连接返回码，Will 消息的 QoS 级别
                false,   // 如果设置为 true，则表示连接请求中包含 Will 消息。
                false,   //是否清理会话
                60    // 保持连接时间（秒）
        );

        // 创建负载部分
        //CommonBody 发送一个
        CommonBody commonBody = new CommonBody(0, "hello");
        String headerStr = "{\"messageId\":\"1234567890\",\"version\":\"1.0\",\"clientId\":\"client123\",\"messageType\":\"DATA\",\"requestType\":\"request\",\"timestamp\":1634567890123,\"traceId\":\"trace123456\",\"interfaceName\":\"login\",\"code\":200}";
        Header header = GsonUtil.fromJsonString(headerStr, Header.class);
        CommonDto<CommonBody> commonDto = new CommonDto<>(header, commonBody);
        String jsonStr = GsonUtil.toJsonString(commonDto);
        //jsonStr转为byte[]
        byte[] bytes = jsonStr.getBytes();
        MqttConnectPayload payload = new MqttConnectPayload(
                CLIENT_ID, // 客户端 ID
                TopicConstant.AGV_CMD_TOPIC,       // Will 消息的主题
                bytes,      // Will 消息的内容
                null,       // 用户名
                null      // 密码
        );

        // 创建连接消息
        return new MqttConnectMessage(fixedHeader, variableHeader, payload);
    }

    private MqttPublishMessage createPublishMessage() {
        // 创建发布消息并加密
        return null;
    }
}
