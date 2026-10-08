package org.pkaq.core.account;

/**
 * 账号创建失败原因，调用模块负责转换其业务错误码。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public class AccountCreationException extends RuntimeException {
    private final Reason reason;

    /** 按明确失败原因构造异常。 */
    public AccountCreationException(Reason reason) {
        super(reason.name());
        this.reason = reason;
    }

    /** 获取失败原因。 */
    public Reason getReason() {
        return reason;
    }

    /** 账号创建失败分类。 */
    public enum Reason {
        INVALID_ACCOUNT,
        MISSING_PASSWORD,
        DUPLICATE_ACCOUNT
    }
}
