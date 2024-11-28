package com.qidi.nettyme.demos.web;

import com.qidi.nettyme.demos.mqtt.dto.Header;
import lombok.Data;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-28 18:11
 */
@Data
public class CommonRequest {
    Header header;
    PubBody body;
}
