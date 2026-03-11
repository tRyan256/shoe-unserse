package com.su.exception;

/**
 * 商品不存在异常
 */
public class ProductNotFoundException extends BaseException {

    public ProductNotFoundException() {
        super();
    }

    public ProductNotFoundException(String msg) {
        super(msg);
    }
}
