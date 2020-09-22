package io.nerv.core.exception;

import io.nerv.core.enums.BizCode;
import io.nerv.core.enums.BizCodeEnum;
import lombok.Getter;

/**
 * 参数错误异常
 * Author: S.PKAQ
 * Datetime: 2018/3/6 14:50
 */
@Getter
public class ParamException extends RuntimeException{
    private String msg;

    public ParamException() {
        super();
        this.msg = BizCodeEnum.PARAM_ERROR.getName();
    }

    public ParamException(String msg) {
        super();
        this.msg = msg;
    }

    public ParamException(BizCode bizCodeEnum) {
        super();
        this.msg = bizCodeEnum.getName();
    }
}
