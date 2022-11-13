package io.nerv.auth.security.domain;

import cn.hutool.core.bean.BeanUtil;
import io.nerv.common.enums.LockEnumm;
import io.nerv.auth.domain.JwtUserDetail;
import io.nerv.sys.web.role.entity.RoleEntity;
import io.nerv.sys.web.user.entity.UserEntity;

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
                LockEnumm.LOCK.getCode().equals(user.getLocked()),
                mapToGrantedAuthorities(user.getRoles())
        );
    }

    private static List<JwtGrantedAuthority> mapToGrantedAuthorities(List<RoleEntity> authorities) {
        return authorities.stream()
                .map(item -> {
                    GrantedRoles grantedRoles = new GrantedRoles();
                    BeanUtil.copyProperties(item, grantedRoles);
                    return new JwtGrantedAuthority(item.getCode(), grantedRoles);
                })
                .collect(Collectors.toList());
    }
}