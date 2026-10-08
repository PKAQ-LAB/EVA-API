package org.pkaq.core.auth.spi;

import org.pkaq.core.auth.spi.model.AccountSnapshot;

/**
 * 认证账号查询端口，不暴露持久化实体或角色查询。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public interface IAccountQuery {
    /**
     * 根据账号、手机号或邮箱读取账号。
     * @param account 登录标识
     * @return 账号快照，不存在返回空
     */
    AccountSnapshot getAccount(String account);

    /**
     * 在已经选择的租户上下文内读取账号。
     * @param account 登录标识
     * @return 账号快照，不存在返回空
     */
    AccountSnapshot getTenantAccount(String account);

    /**
     * 读取认证有效性状态，不加载角色和资源。
     * @param userId 用户编号
     * @return 认证状态，不存在返回空
     */
    AccountSnapshot getAccountState(Long userId);
}
