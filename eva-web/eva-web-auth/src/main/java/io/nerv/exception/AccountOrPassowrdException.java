package io.nerv.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * 自定义鉴权异常类
 */
public class AccountOrPassowrdException extends AuthenticationException {
    public AccountOrPassowrdException(String msg) {
        super(msg);
    }
}
