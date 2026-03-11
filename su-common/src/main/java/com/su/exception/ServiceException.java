package com.su.exception;

/**
 * 服务器内部错误异常 (500)
 */
public class ServiceException extends BaseException {

    public ServiceException() {
        super("服务器内部错误");
    }

    public ServiceException(String msg) {
        super(msg);
    }

}
