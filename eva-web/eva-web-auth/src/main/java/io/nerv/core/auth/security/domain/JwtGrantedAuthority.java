package io.nerv.core.auth.security.domain;

import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

/**
 * 自定义 spring 权限对象
 */
@EqualsAndHashCode
public final class JwtGrantedAuthority implements GrantedAuthority {

    private final String role;

    private GrantedRoles roleEntity;

    public JwtGrantedAuthority(String role) {
        Assert.hasText(role, "A granted authority textual representation is required");
        this.role = role;
    }

    public JwtGrantedAuthority(String role, GrantedRoles roleEntity) {
        Assert.hasText(role, "A granted authority textual representation is required");
        Assert.notNull(roleEntity, "A granted authority textual representation is required");
        this.role = role;
        this.roleEntity = roleEntity;
    }

    @Override
    public String getAuthority() {
        return this.role;
    }

    public GrantedRoles getRoleEntity() {
        return this.roleEntity;
    }

}
