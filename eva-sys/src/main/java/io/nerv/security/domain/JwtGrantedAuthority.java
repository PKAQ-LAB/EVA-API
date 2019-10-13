package io.nerv.security.domain;

import io.nerv.web.sys.role.entity.RoleEntity;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

/**
 * 自定义 spring 权限对象
 */
@EqualsAndHashCode
public final class JwtGrantedAuthority implements GrantedAuthority {

    private final String role;

    private RoleEntity roleEntity;

    public JwtGrantedAuthority(String role) {
        Assert.hasText(role, "A granted authority textual representation is required");
        this.role = role;
    }

    public JwtGrantedAuthority(String role, RoleEntity roleEntity) {
        Assert.hasText(role, "A granted authority textual representation is required");
        Assert.notNull(roleEntity, "A granted authority textual representation is required");
        this.role = role;
        this.roleEntity = roleEntity;
    }

    @Override
    public String getAuthority() {
        return this.role;
    }

    public RoleEntity getRoleEntity() {
        return this.roleEntity;
    }

}
