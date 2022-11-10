package io.nerv.core.exception;

import io.nerv.core.enums.BizCode;
import lombok.Getter;

import java.text.MessageFormat;

/**
 * 关于业务的异常
 */
@Getter
public class BizException extends RuntimeException{
    // 错误码
    private String code;
    // 消息内容
    private String msg;
    // 业务代码
    private BizCode bizCode;

    public BizException(String msg){
        super(msg);
        this.msg=msg;
    }

    public BizException(String code, String msg){
        super(msg);
        this.code = code;
        this.msg = msg;
    }


    public BizException(BizCode bizCode) {
        super(bizCode.getMsg());
        this.code = bizCode.getCode();
        this.msg = bizCode.getMsg();
        this.bizCode = bizCode;
    }

    public BizException(BizCode bizCode, Throwable cause, Object... args) {
        super(bizCode.getMsg(), cause);
        this.code = bizCode.getCode();
        this.msg = MessageFormat.format(this.getMsg(), args);
        this.bizCode = bizCode;
    }

    public BizException(BizCode bizCode, Object... args) {
        super(bizCode.getMsg());
        this.code = bizCode.getCode();
        this.msg = MessageFormat.format( bizCode.getMsg(), args);
        this.bizCode = bizCode;
    }
}
