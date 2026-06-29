package org.pkaq.core.auth.user.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.user.entity.AuthUserEntity;
import org.pkaq.core.auth.user.mapper.AuthUserMapper;
import org.springframework.stereotype.Service;

/**
 * 认证用户服务
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class AuthUserService {

    private final AuthUserMapper authUserMapper;

    /**
     * 获取用户及角色信息
     *
     * @param account 账号/手机号/邮箱
     * @return 用户信息
     */
    public AuthUserEntity getUserWithRoles(String account) {
        AuthUserEntity user = new AuthUserEntity();
        user.setAccount(account);
        return authUserMapper.getUserWithRole(user);
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
    public AuthUserEntity getAuthState(Long userId) {
        if (userId == null || userId <= 0) {
            return null;
        }
        return authUserMapper.getAuthState(userId);
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
