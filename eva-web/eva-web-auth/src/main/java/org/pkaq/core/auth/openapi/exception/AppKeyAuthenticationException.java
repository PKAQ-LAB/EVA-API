package org.pkaq.core.auth.openapi.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * AppKey认证异常
 *
 * @author PKAQ
 */
public class AppKeyAuthenticationException extends AuthenticationException {

    /**
     * 构造认证异常
     *
     * @param message 异常信息
     */
    public AppKeyAuthenticationException(String message) {
        super(message);
    }

    /**
     * 构造认证异常(带原因)
     *
     * @param message 异常信息
     * @param cause 原始异常
     */
    public AppKeyAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
