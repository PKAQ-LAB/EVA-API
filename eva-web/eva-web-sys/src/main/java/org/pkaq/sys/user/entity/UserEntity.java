package org.pkaq.sys.user.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;
import org.pkaq.core.mybatis.mvc.entity.StdTreeEntity;
import org.pkaq.sys.role.entity.RoleEntity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理实体类
 *
 * @author: S.PKAQ
 */
@Data
@Alias("user")
@TableName("sys_user_info")
@EqualsAndHashCode(callSuper = true)
@Schema(title = "用户管理")
public class UserEntity extends StdEntity {

    @Schema(description = "编号")
    private String code;

    @Schema(description = "所属部门")
    private String deptId;

    @Schema(description = "所属部门名称")
    private String deptName;

    @Schema(description = "电话")
    private String tel;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "账号")
    private String account;

    @Schema(description = "密码")
    @JsonIgnore
    private String password;

    @JsonIgnore
    @Schema(description = "盐")
    private String salt;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "姓名")
    @TableField(condition = SqlCondition.LIKE)
    private String name;

    @Schema(description = "昵称")
    private String nickName;

    @Schema(description = "注册ip")
    private String registerIp;

    @Schema(description = "注册时间")
    private Date gmtRegister;

    @Schema(description = "最后登录ip")
    private String lastIp;

    @Schema(description = "最后登录时间")
    private Date lastLogin;

    @Schema(description = "是否锁定")
    private String locked;

    @Schema(description = "所属岗位")
    private String uPostId;

    @Schema(description = "租户code")
    private String tenantCode;

    @Schema(description = "所属岗位名称")
    private String postName;

    @Schema(description = "用户拥有的角色")
    @TableField(exist = false)
    private List<RoleEntity> roles = new ArrayList<>();

    @Schema(description = "用户拥有的模块")
    @TableField(exist = false)
    private List<StdTreeEntity> modules = new ArrayList<>();

}
