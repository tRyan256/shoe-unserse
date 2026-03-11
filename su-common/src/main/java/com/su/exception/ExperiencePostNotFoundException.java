package com.su.exception;

/**
 * 体验心得不存在异常
 */
public class ExperiencePostNotFoundException extends BaseException {

    public ExperiencePostNotFoundException() {
        super();
    }

    public ExperiencePostNotFoundException(String msg) {
        super(msg);
    }
}
