package org.pkaq.core.auth.spi;

import org.pkaq.core.auth.spi.model.AccountProfileSnapshot;

/**
 * 可选管理资料读取端口，认证账号查询不得依赖此端口。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public interface IAccountProfileQuery {
    /**
     * 在可信租户上下文内读取管理资料。
     * @param accountId 账号编号
     * @return 管理资料，不存在返回空
     */
    AccountProfileSnapshot findProfile(Long accountId);
}
