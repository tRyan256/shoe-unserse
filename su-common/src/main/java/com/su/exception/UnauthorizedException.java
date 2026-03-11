package com.su.exception;

/**
 * 未授权异常 (401)
 */
public class UnauthorizedException extends BaseException {

    public UnauthorizedException() {
        super("未登录或登录已过期");
    }

    public UnauthorizedException(String msg) {
        super(msg);
    }

}
