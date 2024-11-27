package com.qidi.nettyme.demos.mqtt.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author maqidi
 * @version 1.0
 * @create 2024-11-04 18:00
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonBody implements Body {
    private Integer code;
    private String msg;
}
