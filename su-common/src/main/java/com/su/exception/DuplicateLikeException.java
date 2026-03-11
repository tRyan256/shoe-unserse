package com.su.exception;

/**
 * 重复点赞异常
 */
public class DuplicateLikeException extends BaseException {

    public DuplicateLikeException() {
        super();
    }

    public DuplicateLikeException(String msg) {
        super(msg);
    }
}
