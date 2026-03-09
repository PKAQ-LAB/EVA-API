package org.pkaq.core.auth.security.domain;

import org.pkaq.core.auth.domain.JwtUserDetail;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.util.BeanUtils;
import org.pkaq.core.util.CollUtils;
import org.pkaq.sys.role.entity.RoleEntity;
import org.pkaq.sys.user.entity.UserEntity;

import java.util.Collections;
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
                user.getId(),
                user.getAccount(),
                user.getPassword(),
                user.getDeptId(),
                user.getName(),
                user.getNickName(),
                FrozenEnumm.FROZEN == user.getFrozen(),
                CollUtils.isEmpty(user.getRoles()) ? Collections.emptyList() : mapToGrantedAuthorities(user.getRoles()));
    }

    private static List<JwtGrantedAuthority> mapToGrantedAuthorities(List<RoleEntity> authorities) {
        return authorities.stream()
                .map(item -> {
                    GrantedRoles grantedRoles = new GrantedRoles();
                    BeanUtils.copyProperties(item, grantedRoles);
                    return new JwtGrantedAuthority(item.getCode(), grantedRoles);
                })
                .collect(Collectors.toList());
    }
}