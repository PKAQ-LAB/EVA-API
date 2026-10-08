package org.pkaq.core.auth.adapter.mybatis.user.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.adapter.mybatis.user.mapper.AuthAccountProfileMapper;
import org.pkaq.core.auth.spi.IAccountProfileQuery;
import org.pkaq.core.auth.spi.model.AccountProfileSnapshot;
import org.springframework.stereotype.Service;

/**
 * 管理资料读取适配器。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Service
@RequiredArgsConstructor
public class MybatisAccountProfileQuery implements IAccountProfileQuery {
    private final AuthAccountProfileMapper profileMapper;

    /**
     * 获取账号管理资料。
     * @param accountId 账号编号
     * @return 可选管理资料
     */
    @Override
    public AccountProfileSnapshot findProfile(Long accountId) {
        return null == accountId || accountId <= 0L ? null : profileMapper.findProfile(accountId);
    }
}
