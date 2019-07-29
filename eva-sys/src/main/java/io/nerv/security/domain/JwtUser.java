package io.nerv.security.domain;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * JwtUser
 * @author: S.PKAQ
 * @Datetime: 2018/4/24 23:14
 */
@Data
public class JwtUser implements UserDetails {
    /**用户ID**/
    private final String id;
    /**用户账号**/
    private final String account;
    /**密码**/
    @JSONField(serialize = false)
    private final String password;
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

    public JwtUser(String id, String account, String password, String deptId, String deptName, String name, String nickName, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.deptId = deptId;
        this.deptName = deptName;
        this.name = name;
        this.nickName = nickName;
        this.authorities = authorities;
    }

    @Override
    public String getUsername() {
        return this.account;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
