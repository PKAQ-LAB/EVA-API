package io.nerv.web.sys.user.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.nerv.core.mvc.entity.tree.BaseTreeEntity;
import io.nerv.web.sys.role.entity.RoleEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import io.nerv.core.mvc.entity.PureBaseEntity;

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
public class UserEntity extends PureBaseEntity {

    @ApiModelProperty("编号")
    private String code;

    @ApiModelProperty("所属部门")
    private String deptId;

    @ApiModelProperty("所属部门名称")
    private String deptName;

    @ApiModelProperty("电话")
    private String tel;

    @ApiModelProperty("邮箱")
    private String email;

    @ApiModelProperty("账号")
    private String account;

    @ApiModelProperty("密码")
    @JSONField(serialize=false)
    private String password;

    @JSONField(serialize=false)
    @ApiModelProperty("盐")
    private String salt;

    @ApiModelProperty("用户头像")
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
    private String locked;

    @ApiModelProperty("用户拥有的角色")
    @TableField(exist = false)
    private List<RoleEntity> roles = new ArrayList<>();

    @ApiModelProperty("用户拥有的模块")
    @TableField(exist = false)
    private List<BaseTreeEntity> modules = new ArrayList<>();

    private static final long serialVersionUID = 1L;

    @TableLogic
    @ApiModelProperty("逻辑删除状态")
    private String deleted;

}
