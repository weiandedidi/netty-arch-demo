package com.qidi.nettyme.demos.tcp.sender;

import com.qidi.nettyme.demos.tcp.dto.Body;
import com.qidi.nettyme.demos.tcp.dto.CommonDto;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-05 16:23
 */
public interface TcpSenderService {
    void sendMessage(String clientId, CommonDto<Body> commonDto);
}
