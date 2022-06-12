package io.nerv.exception;

import io.nerv.core.enums.BizCode;
import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

/**
 * 自定义鉴权异常类
 */
@Getter
public class OathException extends AuthenticationException {
    // 错误码
    private String code;
    // 消息内容
    private String msg;

    public OathException(String msg) {
        super(msg);
    }

    public OathException(BizCode errorCodeEnum) {
        super(errorCodeEnum.getName());
        this.code = errorCodeEnum.getIndex();
        this.msg = errorCodeEnum.getName();
    }
}

