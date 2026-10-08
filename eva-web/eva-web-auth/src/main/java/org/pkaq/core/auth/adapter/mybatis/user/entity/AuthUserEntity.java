package org.pkaq.core.auth.adapter.mybatis.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

import java.util.Date;

/**
 * 账号认证查询投影，不包含管理资料和角色集合。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
@Data
@Alias("authUser")
@TableName("SYS_ACCOUNT")
@EqualsAndHashCode(callSuper = true)
public class AuthUserEntity extends StdEntity {

    /**
     * 账号
     **/
    private String account;

    /**
     * 密码
     **/
    @JsonIgnore
    @ToString.Exclude
    private String password;

    /**
     * 昵称
     **/
    private String nickName;

    /**
     * 权限版本号
     **/
    @TableField("PERM_VER")
    private Long permVer;

    /**
     * 租户冻结状态。
     */
    @TableField(exist = false)
    private FrozenEnumm tenantFrozen;

    /**
     * 租户授权到期时间。
     */
    @TableField(exist = false)
    private Date tenantExpirationDate;
}
