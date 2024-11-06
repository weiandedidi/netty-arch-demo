package com.qidi.nettyme.demos.infrastructure;

/**
 * 错误码的接口类
 *
 * @author maqidi
 * @version 1.0
 * @create 2024-07-09 20:50
 */
public interface ErrorCode {

    /**
     * 获取错误码
     *
     * @return
     */
    int getCode();

    /**
     * 获取错误信息
     *
     * @return
     */
    String getDescription();

}
