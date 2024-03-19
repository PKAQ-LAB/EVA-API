package tech.yunyue.core.exception;

import lombok.Data;
import tech.yunyue.core.enums.BizCode;

/**
 * 用户认证授权异常
 */
@Data
public class UnauthorizedException extends RuntimeException {
    /**
     * 消息枚举
     */
    private BizCode bizCode;

    public UnauthorizedException(BizCode bizCode) {
        super(bizCode.getMsg());
        this.bizCode = bizCode;
    }
}
