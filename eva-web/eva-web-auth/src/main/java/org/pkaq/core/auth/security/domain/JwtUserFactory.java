package org.pkaq.core.auth.security.domain;

import cn.hutool.core.bean.BeanUtil;
import org.pkaq.core.auth.domain.JwtUserDetail;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.user.entity.UserEntity;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JwtUser 工厂
 *
 * @author PKAQ
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
                user.getName(),
                user.getNickName(),
                LockEnumm.LOCK.getCode().equals(user.getFrozen()),
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