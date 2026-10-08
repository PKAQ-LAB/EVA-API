package org.pkaq.core.account;

/**
 * 不依赖系统管理模块的统一账号创建契约。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public interface IAccountCreation {
    /** 校验账号合法性，失败抛出账号创建异常。 */
    void validateAccount(String account);

    /** 在当前可信租户范围创建账号，不附加管理资料和角色。 */
    Long create(AccountCreationCommand command);
}
