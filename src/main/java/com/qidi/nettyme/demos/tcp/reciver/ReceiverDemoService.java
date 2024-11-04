package com.qidi.nettyme.demos.tcp.reciver;

import com.qidi.nettyme.demos.tcp.dto.Body;
import com.qidi.nettyme.demos.tcp.dto.CommonDto;

/**
 * @author qidi
 * @date 2019-04-25 10:59
 */
public interface ReceiverDemoService {
    CommonDto<Body> parseMessage(String message);
}
