package io.nerv.security.exception;

import io.nerv.core.enums.ErrorCodeEnum;
import org.springframework.security.core.AuthenticationException;

/**
 * 自定义鉴权异常类
 */
public class OathException extends AuthenticationException {

    public OathException(String msg) {
        super(msg);
    }

    public OathException(ErrorCodeEnum errorCodeEnum) {
        super(errorCodeEnum.getName());
    }
}
