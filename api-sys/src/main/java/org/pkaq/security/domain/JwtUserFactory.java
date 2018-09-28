package org.pkaq.security.domain;

import org.pkaq.web.sys.role.entity.RoleEntity;
import org.pkaq.web.sys.user.entity.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.stream.Collectors;

public final class JwtUserFactory {

    private JwtUserFactory() {
    }

    public static JwtUser create(UserEntity user) {
        return new JwtUser(
                String.valueOf(user.getId()),
                user.getAccount(),
                user.getPassword(),
                mapToGrantedAuthorities(user.getRoles().stream().map(RoleEntity::getCode).collect(Collectors.toList()))
        );
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(List<String> authorities) {
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }
}