package io.nerv.core.auth.security.service;

import io.nerv.biz.sys.user.entity.UserEntity;
import io.nerv.biz.sys.user.mapper.UserMapper;
import io.nerv.core.auth.security.domain.JwtUserFactory;
import io.nerv.core.enums.BizCodeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义用户认证
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    /**
     * 提供一种从用户名可以查到用户并返回的方法
     *
     * @param account
     * @return UserDetails
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String account) {

        UserEntity user = new UserEntity();
        user.setAccount(account);
        user.setTel(account);
        user.setEmail(account);

        user = userMapper.getUserWithRole(user);

        BizCodeEnum.ACCOUNT_NOT_EXIST.assertNotNull(user);
        return JwtUserFactory.create(user);
    }
}