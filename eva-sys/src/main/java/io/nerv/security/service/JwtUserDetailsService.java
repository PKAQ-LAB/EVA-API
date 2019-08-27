package io.nerv.security.service;

import io.nerv.core.enums.ErrorCodeEnum;
import io.nerv.security.domain.JwtUserFactory;
import io.nerv.security.exception.OathException;
import io.nerv.web.sys.user.entity.UserEntity;
import io.nerv.web.sys.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义用户认证
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 提供一种从用户名可以查到用户并返回的方法
     * @param id userid
     * @return UserDetails
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String id) {

        UserEntity user = userMapper.getUserWithRoleById(id);

        if (user == null) {
            throw new OathException(ErrorCodeEnum.ACCOUNT_NOT_EXIST);
        } else {
            return JwtUserFactory.create(user);
        }
    }
}