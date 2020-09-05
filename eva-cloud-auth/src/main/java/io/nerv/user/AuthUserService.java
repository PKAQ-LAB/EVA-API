package io.nerv.user;

import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.exception.OathException;
import io.nerv.web.sys.user.entity.UserEntity;
import io.nerv.web.sys.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthUserService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String account) throws UsernameNotFoundException {
        UserEntity user = new UserEntity();
        user.setAccount(account);
        user.setTel(account);
        user.setEmail(account);

        user = userMapper.getUserWithRole(user);

        if (user == null) {
            throw new OathException(BizCodeEnum.ACCOUNT_NOT_EXIST);
        } else {
            return AuthUserFactory.create(user);
        }

    }
}