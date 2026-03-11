package com.su.exception;

/**
 * 重复关注异常
 */
public class DuplicateFollowException extends BaseException {

    public DuplicateFollowException() {
    }

    public DuplicateFollowException(String msg) {
        super(msg);
    }
}
