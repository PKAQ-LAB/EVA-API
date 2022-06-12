package io.nerv.security.domain;

import io.nerv.core.enums.LockEnumm;
import io.nerv.domain.JwtUserDetail;
import io.nerv.web.sys.role.entity.RoleEntity;
import io.nerv.web.sys.user.entity.UserEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JwtUser 工厂
 */
public final class JwtUserFactory {

    private JwtUserFactory() {
    }

    public static JwtUserDetail create(UserEntity user) {
        return new JwtUserDetail(
                String.valueOf(user.getId()),
                user.getAccount(),
                user.getPassword(),
                user.getDeptId(),
                user.getDeptName(),
                user.getName(),
                user.getNickName(),
                LockEnumm.LOCK.getIndex().equals(user.getLocked()),
                mapToGrantedAuthorities(user.getRoles())
        );
    }

    private static List<JwtGrantedAuthority> mapToGrantedAuthorities(List<RoleEntity> authorities) {
        return authorities.stream()
                .map(item -> new JwtGrantedAuthority(item.getCode(), item))
                .collect(Collectors.toList());
    }
}