package io.nerv.weixin.exception;

/**
 * 微信异常类
 */
public class WeixinException extends Exception {
    public WeixinException(String msg) {
        super(msg);
    }

    public WeixinException(BizCode bizCode) {
        super(bizCode.name);
    }
}
