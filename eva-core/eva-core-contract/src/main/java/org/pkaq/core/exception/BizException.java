package org.pkaq.core.exception;

import lombok.Getter;
import org.pkaq.core.codes.BizCode;

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

    private String estr;

    private Object data;

    private Object[] args;

    public BizException(String msg) {
        super(msg);
    }

    public BizException(String msg, String estr) {
        super(msg);
        this.estr = estr;
    }

    public BizException(BizCode bizCode) {
        super(bizCode.getMsg());
        this.bizCode = bizCode;
    }

    public BizException(BizCode bizCode, Object data) {
        super(bizCode.getMsg());
        this.bizCode = bizCode;
        this.data = data;
    }

    public BizException(BizCode bizCode, Throwable cause, Object... args) {
        super(MessageFormat.format(bizCode.getMsg(), args), cause);
        this.args = args;
        this.bizCode = bizCode;
    }

    public BizException(BizCode bizCode, Object... args) {
        super(MessageFormat.format(bizCode.getMsg(), args));
        this.args = args;
        this.bizCode = bizCode;
    }
}
