package org.pkaq.sys.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.mapper.SqlCondition;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.BaseEntity;
import org.pkaq.sys.module.entity.ModuleEntity;
import org.pkaq.sys.role.entity.RoleEntity;

import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理实体类
 * @author: S.PKAQ
 * @Datetime: 2018/3/29 23:58
 */
@Data
@Alias("user")
@TableName("sys_user_info")
@EqualsAndHashCode(callSuper = true)
public class UserEntity extends BaseEntity implements Serializable {

    private String code;

    private String deptId;

    @TableField(condition = SqlCondition.LIKE)
    private String tel;

    private String email;

    @TableField(condition = SqlCondition.LIKE)
    private String account;

    @JSONField(serialize=false)
    private String password;
    @JSONField(serialize=false)
    private String salt;

    private String avatar;

    @TableField(condition = SqlCondition.LIKE)
    private String name;

    private String nickName;

    private String registerIp;

    private Date gmtRegister;

    private String lastIp;

    private Date lastLogin;

    private Boolean locked;

    /**用户拥有的角色**/
    @TableField(exist = false)
    private List<RoleEntity> roles = new ArrayList<>();

    /**用户拥有的模块**/
    @TableField(exist = false)
    private List<ModuleEntity> modules = new ArrayList<>();

    private static final long serialVersionUID = 1L;

}
