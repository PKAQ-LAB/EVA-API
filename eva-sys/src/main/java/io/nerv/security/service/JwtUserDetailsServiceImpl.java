package io.nerv.security.service;

import io.nerv.security.domain.JwtUserFactory;
import io.nerv.web.sys.user.entity.UserEntity;
import io.nerv.web.sys.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 提供一种从用户名可以查到用户并返回的方法
     * @param id userid
     * @return UserDetails
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {

        UserEntity user = userMapper.getUserWithRoleById(id);

        if (user == null) {
            throw new UsernameNotFoundException(String.format("找不到该用户 '%s'.", id));
        } else {
            return JwtUserFactory.create(user);
        }
    }
}