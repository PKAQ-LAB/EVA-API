package org.pkaq.core.auth.security.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.AuthCodes;
import org.pkaq.core.auth.security.domain.JwtUserFactory;
import org.pkaq.sys.user.entity.UserEntity;
import org.pkaq.sys.user.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 鑷畾涔夌敤鎴疯璇?
 *
 * @author PKAQ
 */
@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    /**
     * 鎻愪緵涓€绉嶄粠鐢ㄦ埛鍚嶅彲浠ユ煡鍒扮敤鎴峰苟杩斿洖鐨勬柟娉?
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

        var u = userMapper.getUserWithRole(user);

        AuthCodes.ACCOUNT_OR_PWD_ERROR.assertNotNull(u);
        return JwtUserFactory.create(u);
    }
}
