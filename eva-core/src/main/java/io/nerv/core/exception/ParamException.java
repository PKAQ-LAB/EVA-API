package io.nerv.core.exception;

import lombok.Getter;

/**
 * 参数错误异常
 * Author: S.PKAQ
 * Datetime: 2018/3/6 14:50
 */
@Getter
public class ParamException extends RuntimeException{
    private String msg;

    public ParamException(String msg) {
        super();
        this.msg = msg;
    }
}
