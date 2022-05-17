package io.nerv.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * JwtUser
 * @author: S.PKAQ
 */
@Data
public class SecurityUserDetails implements UserDetails {
    /**用户ID**/
    private final String id;
    /**用户账号**/
    private final String account;
    /**密码**/
    @JsonIgnore
    private final String password;
    /****/
    private String roleId;

    /**用户是否已经锁定**/
    private boolean accountNonLocked;

    /**权限集合**/
    private Collection<? extends GrantedAuthority> authorities;
    public SecurityUserDetails(String id, String account, String password, String roleId, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.roleId = roleId;
        this.accountNonLocked = accountNonLocked;
        this.authorities = authorities;
    }

    @Override
    public String getUsername() {
        return this.account;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
