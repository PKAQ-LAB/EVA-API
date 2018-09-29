package org.pkaq.pay.alipay.exception;

/**
 * 支付宝异常类
 */
public class AlipayException extends RuntimeException{
    private String msg;

    public AlipayException(String msg) {
        super();
        this.msg = msg;
    }
}