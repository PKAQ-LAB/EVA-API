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
    /**客户端ID**/
    private final String clientId;
    /**密码**/
    @JsonIgnore
    private final String password;
    /**用户是否已经锁定**/
    private boolean accountNonLocked;
    /**部门id**/
    private String deptId;
    /**部门名称**/
    private String deptName;
    /**用户姓名**/
    private String name;
    /**用户昵称**/
    private String nickName;
    /**权限集合**/
    private final Collection<? extends GrantedAuthority> authorities;

    public SecurityUserDetails(String id, String account, String clientId, String password, String deptId, String deptName, String name, String nickName, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.account = account;
        this.clientId = clientId;
        this.password = password;
        this.deptId = deptId;
        this.deptName = deptName;
        this.name = name;
        this.nickName = nickName;
        this.accountNonLocked = !accountNonLocked;
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
