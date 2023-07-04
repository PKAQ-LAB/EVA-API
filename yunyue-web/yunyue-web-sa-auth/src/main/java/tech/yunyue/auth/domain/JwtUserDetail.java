package tech.yunyue.auth.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import tech.yunyue.core.threaduser.ThreadUser;

import java.util.Map;

/**
 * JwtUser
 *
 * @author: S.PKAQ
 * @Datetime: 2018/4/24 23:14
 */
@Data
@AllArgsConstructor
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
    /** 集团租户号 */
    private String tenantId;
    /** 公司租户号 */
    private String companyTenantId;
    /**
     * 岗位id
     **/
    private String postId;
    /**
     * 岗位名称
     **/
    private String postName;
    /**
     * 权限集合
     **/
    private final Map<String, ThreadUser.GrantedRoles> authorities;
}
