package io.nerv.core.exception;

import io.nerv.core.enums.BizCode;
import lombok.Getter;

import java.text.MessageFormat;

/**
 * 关于业务的异常
 *
 * @author PKAQ
 */
@Getter
public class BizException extends RuntimeException {

    /**
     * 消息枚举
     */
    private BizCode bizCode;

    public BizException(String msg) {
        super(msg);
    }

    public BizException(BizCode bizCode) {
        super(bizCode.getMsg());
        this.bizCode = bizCode;
    }

    public BizException(BizCode bizCode, Throwable cause, Object... args) {
        super(MessageFormat.format(bizCode.getMsg(), args), cause);
        this.bizCode = bizCode;
    }

    public BizException(BizCode bizCode, Object... args) {
        super(MessageFormat.format(bizCode.getMsg(), args));
        this.bizCode = bizCode;
    }
}
