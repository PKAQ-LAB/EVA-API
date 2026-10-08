package org.pkaq.core.mybatis.account.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

import java.sql.Date;

/**
 * 唯一账号持久化模型，不包含组织和管理档案。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Data
@Alias("account")
@TableName(value = "SYS_ACCOUNT", excludeProperty = "tenantId")
@EqualsAndHashCode(callSuper = true)
public class AccountEntity extends StdEntity {
    private String account;
    @JsonIgnore
    @ToString.Exclude
    private String password;
    private String avatar;
    @TableField(condition = SqlCondition.LIKE)
    private String nickName;
    @TableField(condition = SqlCondition.LIKE)
    private String tel;
    private String email;
    private String lastIp;
    private Date lastLogin;
    private Long permVer;
}
