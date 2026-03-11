package com.su.exception;

/**
 * 请求参数错误异常 (400)
 */
public class BadRequestException extends BaseException {

    public BadRequestException() {
        super("请求参数错误");
    }

    public BadRequestException(String msg) {
        super(msg);
    }

}
