package org.pkaq.web.sys.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.PureBaseEntity;
import org.pkaq.web.sys.module.entity.ModuleEntity;
import org.pkaq.web.sys.role.entity.RoleEntity;

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
@ApiModel("用户管理")
public class UserEntity extends PureBaseEntity implements Serializable {
    @ApiModelProperty("编号")
    private String code;
    @ApiModelProperty("所属部门")
    private String deptId;
    @ApiModelProperty("电话")
    @TableField(condition = SqlCondition.LIKE)
    private String tel;
    @ApiModelProperty("邮箱")
    private String email;
    @ApiModelProperty("账号")
    @TableField(condition = SqlCondition.LIKE)
    private String account;
    @ApiModelProperty("密码")
    @JSONField(serialize=false)
    private String password;
    @JSONField(serialize=false)
    @ApiModelProperty("盐")
    private String salt;
    @ApiModelProperty("用户编码")
    private String avatar;
    @ApiModelProperty("姓名")
    @TableField(condition = SqlCondition.LIKE)
    private String name;
    @ApiModelProperty("昵称")
    private String nickName;
    @ApiModelProperty("注册ip")
    private String registerIp;
    @ApiModelProperty("注册时间")
    private Date gmtRegister;
    @ApiModelProperty("最后登录ip")
    private String lastIp;
    @ApiModelProperty("最后登录时间")
    private Date lastLogin;
    @ApiModelProperty("是否锁定")
    private Boolean locked;
    @ApiModelProperty("用户拥有的角色")
    @TableField(exist = false)
    private List<RoleEntity> roles = new ArrayList<>();
    @ApiModelProperty("用户拥有的模块")
    @TableField(exist = false)
    private List<ModuleEntity> modules = new ArrayList<>();

    private static final long serialVersionUID = 1L;

}
