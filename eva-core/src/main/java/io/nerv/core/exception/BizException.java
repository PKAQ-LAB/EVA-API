package io.nerv.core.exception;

import io.nerv.core.enums.BizCodeEnum;
import lombok.Getter;

/**
 * 关于业务的异常
 */
@Getter
public class BizException extends RuntimeException{
    // 错误码
    private int code;
    // 消息内容
    private String msg;
    // 业务代码
    private BizCodeEnum bizCodeEnum;

    public BizException(String msg){
        super(msg);
        this.msg=msg;
    }

    public BizException(int code, String msg){
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public BizException(BizCodeEnum resourceUsed) {
        super(resourceUsed.getName());
        this.code = resourceUsed.getIndex();
        this.msg = resourceUsed.getName();
        this.bizCodeEnum = resourceUsed;
    }
}
