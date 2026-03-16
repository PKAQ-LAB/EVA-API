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
}
