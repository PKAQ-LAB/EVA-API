package org.pkaq.core.auth.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.auth.role.entity.AuthRoleEntity;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

import java.util.List;

/**
 * 认证用户实体
 *
 * @author PKAQ
 */
@Data
@Alias("authUser")
@TableName("SYS_USER")
@EqualsAndHashCode(callSuper = true)
public class AuthUserEntity extends StdEntity {

    /**
     * 账号
     **/
    private String account;

    /**
     * 密码
     **/
    private String password;

    /**
     * 姓名
     **/
    private String name;

    /**
     * 昵称
     **/
    private String nickName;

    /**
     * 所属部门
     **/
    private Long deptId;

    /**
     * 用户拥有的角色
     **/
    @TableField(exist = false)
    private List<AuthRoleEntity> roles;

    /**
     * 权限版本号
     **/
    @TableField("PERM_VER")
    private Long permVer;
}
