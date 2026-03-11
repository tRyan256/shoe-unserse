package com.su.exception;

/**
 * 权限不足异常 (403)
 */
public class ForbiddenException extends BaseException {

    public ForbiddenException() {
        super("权限不足");
    }

    public ForbiddenException(String msg) {
        super(msg);
    }

}
