package com.su.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一错误响应格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse implements Serializable {

    /**
     * HTTP状态码
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 请求路径
     */
    private String path;

    public ErrorResponse(Integer code, String message, String path) {
        this.code = code;
        this.message = message;
        this.timestamp = System.currentTimeMillis() / 1000;
        this.path = path;
    }

}
