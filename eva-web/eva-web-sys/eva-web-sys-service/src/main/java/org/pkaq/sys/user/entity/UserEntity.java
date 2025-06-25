package org.pkaq.sys.user.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

import java.sql.Date;

/**
 * 用户管理实体类
 *
 * @author: S.PKAQ
 */
@Data
@Alias("user")
@TableName("SYS_USER")
@EqualsAndHashCode(callSuper = true)
public class UserEntity extends StdEntity {

    /**
     * 编号
     **/
    private String code;

    /**
     * 账号
     **/
    private String account;

    /**
     * 密码
     **/
    private String password;

    /**
     * 盐
     **/
    private String salt;

    /**
     * 用户头像
     **/
    private String avatar;

    /**
     * 姓名
     **/
    @TableField(condition = SqlCondition.LIKE)
    private String name;

    /**
     * 昵称
     **/
    @TableField(condition = SqlCondition.LIKE)
    private String nickName;

    /**
     * 电话
     **/
    @TableField(condition = SqlCondition.LIKE)
    private String tel;

    /**
     * 邮箱
     **/
    private String email;

    /**
     * 最后登录ip
     **/
    private String lastIp;

    /**
     * 最后登录时间
     **/
    private Date lastLogin;

    /**
     * 所属部门
     **/
    private Long deptId;

}
