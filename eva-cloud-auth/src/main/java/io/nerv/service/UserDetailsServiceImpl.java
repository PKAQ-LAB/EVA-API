package io.nerv.service;

import io.nerv.domain.SecurityUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户管理业务类
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;


    /**
     * 登录判断逻辑
     * 提供一种从用户名可以查到用户并返回的方法
     * @param username the username identifying the user whose data is required.
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_ADMIN");


        SecurityUserDetails user = new SecurityUserDetails("1", "admin", "clientId", passwordEncoder.encode("123456"), "1", "统合部", "wu", "pgee", false, List.of(grantedAuthority));
        return user;
    }

}
