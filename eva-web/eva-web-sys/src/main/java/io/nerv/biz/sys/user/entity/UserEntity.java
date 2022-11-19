package io.nerv.biz.sys.user.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nerv.biz.sys.role.entity.RoleEntity;
import io.nerv.core.mybatis.mvc.entity.mybatis.StdEntity;
import io.nerv.core.mybatis.mvc.entity.mybatis.StdTreeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

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

    @Schema(description = "微信id")
    private String weixin_id;

    @Schema(description = "用户拥有的角色")
    @TableField(exist = false)
    private List<RoleEntity> roles = new ArrayList<>();

    @Schema(description = "用户拥有的模块")
    @TableField(exist = false)
    private List<StdTreeEntity> modules = new ArrayList<>();

    @TableLogic
    @Schema(description = "逻辑删除状态")
    private String deleted;

}
