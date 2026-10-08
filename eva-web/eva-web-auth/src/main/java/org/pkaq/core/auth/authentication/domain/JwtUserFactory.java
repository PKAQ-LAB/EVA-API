package org.pkaq.core.auth.authentication.domain;

import org.pkaq.core.auth.spi.model.RoleSnapshot;
import org.pkaq.core.auth.spi.model.AccountSnapshot;
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

    public static JwtUserDetail create(AccountSnapshot user, List<RoleSnapshot> roles) {
        var detail = new JwtUserDetail(
                user.getId(),
                user.getAccount(),
                user.getTenantId(),
                user.getPassword(),
                user.getDeptId(),
                user.getName(),
                user.getNickName(),
                FrozenEnumm.FROZEN == user.getFrozen(),
                CollUtils.isEmpty(roles) ? Collections.emptyList() : mapToGrantedAuthorities(roles));

        // 设置角色ID列表和权限版本号（用于写入JWT）
        if (CollUtils.isNotEmpty(roles)) {
            detail.setRoleIds(roles.stream().map(RoleSnapshot::getId).toList());
        }
        detail.setPermVer(user.getPermVer() != null ? user.getPermVer() : 0L);

        return detail;
    }

    private static List<JwtGrantedAuthority> mapToGrantedAuthorities(List<RoleSnapshot> authorities) {
        return authorities.stream()
                .map(item -> {
                    GrantedRoles grantedRoles = new GrantedRoles();
                    BeanUtils.copyProperties(item, grantedRoles);
                    return new JwtGrantedAuthority(item.getCode(), grantedRoles);
                })
                .collect(Collectors.toList());
    }
}
