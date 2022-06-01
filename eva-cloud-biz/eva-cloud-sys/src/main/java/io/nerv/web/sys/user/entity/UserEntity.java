package io.nerv.web.sys.user.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nerv.core.mvc.entity.mybatis.BaseTreeEntity;
import io.nerv.core.mvc.entity.mybatis.StdBaseEntity;
import io.nerv.web.sys.role.entity.RoleEntity;
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
public class UserEntity extends StdBaseEntity {

    @Schema(name = "编号")
    private String code;

    @Schema(name = "所属部门")
    private String deptId;

    @Schema(name = "所属部门名称")
    private String deptName;

    @Schema(name = "电话")
    private String tel;

    @Schema(name = "邮箱")
    private String email;

    @Schema(name = "账号")
    private String account;

    @Schema(name = "密码")
    @JsonIgnore
    private String password;

    @JsonIgnore
    @Schema(name = "盐")
    private String salt;

    @Schema(name = "用户头像")
    private String avatar;

    @Schema(name = "姓名")
    @TableField(condition = SqlCondition.LIKE)
    private String name;

    @Schema(name = "昵称")
    private String nickName;

    @Schema(name = "注册ip")
    private String registerIp;

    @Schema(name = "注册时间")
    private Date gmtRegister;

    @Schema(name = "最后登录ip")
    private String lastIp;

    @Schema(name = "最后登录时间")
    private Date lastLogin;

    @Schema(name = "是否锁定")
    private String locked;

    @Schema(name = "微信id")
    private String weixin_id;

    @Schema(name = "用户拥有的角色")
    @TableField(exist = false)
    private List<RoleEntity> roles = new ArrayList<>();

    @Schema(name = "用户拥有的模块")
    @TableField(exist = false)
    private List<BaseTreeEntity> modules = new ArrayList<>();

    @TableLogic
    @Schema(name = "逻辑删除状态")
    private String deleted;

}
