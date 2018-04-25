package org.pkaq.security.service;

import org.pkaq.security.domain.JwtUserFactory;
import org.pkaq.web.sys.user.entity.UserEntity;
import org.pkaq.web.sys.user.mapper.UserMapper;
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
     * @param account 帐号
     * @return UserDetails
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        UserEntity userEntity = new UserEntity();
        userEntity.setAccount(account);

        UserEntity user = userMapper.selectOne(userEntity);

        if (user == null) {
            throw new UsernameNotFoundException(String.format("No user found with username '%s'.", account));
        } else {
            return JwtUserFactory.create(user);
        }
    }
}