package com.su.exception;

/**
 * 业务冲突异常 (409)
 */
public class ConflictException extends BaseException {

    public ConflictException() {
        super("业务冲突");
    }

    public ConflictException(String msg) {
        super(msg);
    }

}
