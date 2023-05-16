package tech.yunyue.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Map;

/**
 * JwtUser
 *
 * @author: S.PKAQ
 * @Datetime: 2018/4/24 23:14
 */
@Data
public class JwtUserDetail{
    /**
     * 用户ID
     **/
    private final String id;
    /**
     * 用户账号
     **/
    private final String account;
    /**
     * 密码
     **/
    @JsonIgnore
    private final String password;
    /**
     * 用户是否已经锁定
     **/
    private boolean accountNonLocked;
    /**
     * 部门id
     **/
    private String deptId;
    /**
     * 部门名称
     **/
    private String deptName;
    /**
     * 用户姓名
     **/
    private String name;
    /**
     * 用户昵称
     **/
    private String nickName;
    /**
     * 权限集合
     **/
    private final Map<String,GrantedRoles> authorities;

    public JwtUserDetail(String id, String account, String password, String deptId, String deptName, String name, String nickName, boolean accountNonLocked, Map<String,GrantedRoles> authorities) {
        this.id = id;
        this.account = account;
        this.password = password;
        this.deptId = deptId;
        this.deptName = deptName;
        this.name = name;
        this.nickName = nickName;
        this.accountNonLocked = !accountNonLocked;
        this.authorities = authorities;
    }

    public String getUsername() {
        return this.account;
    }

}
