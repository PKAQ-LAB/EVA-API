package org.pkaq.core.auth.security.domain;

import org.pkaq.core.auth.domain.JwtUserDetail;
import org.pkaq.core.auth.role.entity.AuthRoleEntity;
import org.pkaq.core.auth.user.entity.AuthUserEntity;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.util.BeanUtils;
import org.pkaq.core.util.CollUtils;

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

    public static JwtUserDetail create(AuthUserEntity user) {
        var detail = new JwtUserDetail(
                user.getId(),
                user.getAccount(),
                user.getPassword(),
                user.getDeptId(),
                user.getName(),
                user.getNickName(),
                FrozenEnumm.FROZEN == user.getFrozen(),
                CollUtils.isEmpty(user.getRoles()) ? Collections.emptyList() : mapToGrantedAuthorities(user.getRoles()));

        // 设置角色ID列表和权限版本号（用于写入JWT）
        if (CollUtils.isNotEmpty(user.getRoles())) {
            detail.setRoleIds(user.getRoles().stream().map(AuthRoleEntity::getId).toList());
        }
        detail.setPermVer(user.getPermVer() != null ? user.getPermVer() : 0L);

        return detail;
    }

    private static List<JwtGrantedAuthority> mapToGrantedAuthorities(List<AuthRoleEntity> authorities) {
        return authorities.stream()
                .map(item -> {
                    GrantedRoles grantedRoles = new GrantedRoles();
                    BeanUtils.copyProperties(item, grantedRoles);
                    return new JwtGrantedAuthority(item.getCode(), grantedRoles);
                })
                .collect(Collectors.toList());
    }
}
