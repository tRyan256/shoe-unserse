package com.su.exception;

/**
 * 资源不存在异常 (404)
 */
public class NotFoundException extends BaseException {

    public NotFoundException() {
        super("资源不存在");
    }

    public NotFoundException(String msg) {
        super(msg);
    }

}
