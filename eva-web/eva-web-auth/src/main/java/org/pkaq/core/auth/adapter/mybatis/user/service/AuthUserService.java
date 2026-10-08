package org.pkaq.core.auth.adapter.mybatis.user.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.adapter.mybatis.user.entity.AuthUserEntity;
import org.pkaq.core.auth.adapter.mybatis.user.mapper.AuthUserMapper;
import org.pkaq.core.auth.spi.IAccountQuery;
import org.pkaq.core.auth.spi.model.AccountSnapshot;
import org.pkaq.core.util.BeanUtils;
import org.springframework.stereotype.Service;
import org.pkaq.core.properties.EvaConfig;

/**
 * 只读账号查询的 MyBatis 适配器。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Service
@RequiredArgsConstructor
public class AuthUserService implements IAccountQuery {

    private final AuthUserMapper authUserMapper;
    private final EvaConfig evaConfig;

    /**
     * 获取账号投影，不加载角色信息。
     *
     * @param account 账号/手机号/邮箱
     * @return 用户信息
     */
    @Override
    public AccountSnapshot getAccount(String account) {
        AuthUserEntity user = new AuthUserEntity();
        user.setAccount(account);
        return toSnapshot(authUserMapper.getUserAccount(user));
    }

    /**
     * 获取已选择租户中的账号投影，不加载角色。
     *
     * @param account 账号、手机号或邮箱
     * @return 账号快照
     */
    @Override
    public AccountSnapshot getTenantAccount(String account) {
        AuthUserEntity user = new AuthUserEntity();
        user.setAccount(account);
        return toSnapshot(authUserMapper.getTenantUserAccount(user));
    }

    /**
     * 获取用户的权限版本号
     *
     * @param userId 用户ID
     * @return 权限版本号
     */
    public Long getPermVer(Long userId) {
        Long ver = authUserMapper.getPermVer(userId);
        return ver != null ? ver : 0L;
    }

    /**
     * 获取认证期用户状态。
     *
     * @param userId 用户ID
     * @return 用户认证状态
     */
    @Override
    public AccountSnapshot getAccountState(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        AuthUserEntity authState = evaConfig.getTenant().isSchemaMode()
                ? authUserMapper.getTenantAuthState(userId)
                : authUserMapper.getAuthState(userId);
        return toSnapshot(authState);
    }

    private AccountSnapshot toSnapshot(AuthUserEntity entity) {
        if (null == entity) {
            return null;
        }
        AccountSnapshot snapshot = new AccountSnapshot();
        BeanUtils.copyProperties(entity, snapshot);
        return snapshot;
    }

    /**
     * 自增用户的权限版本号（角色变更时调用）
     *
     * @param userId 用户ID
     */
    public void incrementPermVer(Long userId) {
        authUserMapper.incrementPermVer(userId);
    }
}
