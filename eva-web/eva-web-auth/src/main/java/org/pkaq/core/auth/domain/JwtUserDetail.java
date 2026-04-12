package org.pkaq.core.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * JWT用户详情
 *
 * @author PKAQ
 */
@Data
public class JwtUserDetail implements UserDetails {
    /**
     * 用户ID
     */
    private final Long id;
    /**
     * 用户账号
     */
    private final String account;
    /**
     * 密码
     */
    @JsonIgnore
    private final String password;
    /**
     * 权限集合
     */
    private final Collection<? extends GrantedAuthority> authorities;
    /**
     * 是否未锁定
     */
    @JsonIgnore
    private boolean accountNonLocked;
    /**
     * 部门ID
     */
    private Long deptId;
    /**
     * 部门名称
     */
    private String deptName;
    /**
     * 用户姓名
     */
    private String name;
    /**
     * 用户昵称
     */
    private String nickName;
    /**
     * 角色ID列表（写入JWT）
     */
    @JsonIgnore
    private List<Long> roleIds;
    /**
     * 权限版本号（写入JWT）
     */
    @JsonIgnore
    private Long permVer;

    public JwtUserDetail(Long id,
                         String account,
                         String password,
                         Long deptId,
                         String name,
                         String nickName,
                         boolean accountNonLocked,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.deptId = deptId;
        this.name = name;
        this.nickName = nickName;
        this.accountNonLocked = !accountNonLocked;
        this.authorities = authorities;
    }

    @Override
    public String getUsername() {
        return this.account;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }
}