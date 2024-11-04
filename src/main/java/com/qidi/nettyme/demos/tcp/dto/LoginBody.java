package com.qidi.nettyme.demos.tcp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 作为aoo请求的body
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-11-04 17:51
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginBody implements Body{
    /**
     * 设备id
     */
    private String machineId;
    /**
     * 模块
     */
    private String model;
}
