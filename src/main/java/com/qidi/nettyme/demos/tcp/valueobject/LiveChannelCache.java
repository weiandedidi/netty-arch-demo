package com.qidi.nettyme.demos.tcp.valueobject;

import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by RoyDeng on 18/2/4.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveChannelCache {
    /**
     * 唯一id
     */
    private String clientId;
    private Channel channel;
    private boolean authenticated;
    private long connectionTime;
    private String clientIp;
}
