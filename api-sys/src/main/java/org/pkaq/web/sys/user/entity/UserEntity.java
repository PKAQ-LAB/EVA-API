package org.pkaq.web.sys.user.entity;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.BaseEntity;

import java.io.Serializable;
import java.sql.Date;

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

    private String tel;

    private String email;

    private String account;

    private String password;

    private String salt;

    private String avatar;

    private String name;

    private String nickName;

    private String registerIp;

    private Date gmtRegister;

    private String lastIp;

    private Date lastLogin;

    private Byte locked;


    private static final long serialVersionUID = 1L;

}
