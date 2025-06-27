package org.pkaq.sys.tenant.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

import java.util.Date;

/**
 * 租户管理
 *
 * @author PKAQ
 */
@Data
@Alias("tenantEntity")
@TableName("sys_tenant")
@EqualsAndHashCode(callSuper = true)
public class TenantEntity extends StdEntity {
    /**
     * 租户名称
     **/
    @TableField(condition = SqlCondition.LIKE)
    private String name;

    /**
     * 租户编码
     **/
    @TableField(condition = SqlCondition.LIKE)
    private String code;

    /**
     * 租户类型
     **/
    private String type;

    /**
     * 全称
     **/
    private String fullName;

    /**
     * 证件类型
     **/
    private String cardType;

    /**
     * 证件号
     **/
    @TableField(condition = SqlCondition.LIKE)
    private String cardNo;

    /**
     * 联系人
     **/
    private String contactName;

    /**
     * 联系方式
     **/
    private String contactTel;

    /**
     * 授权用户数
     **/
    private int authUserCount;

    /**
     * 到期时间
     **/
    private Date expirationDate;

    /**
     * 管理员Id
     **/
    private Long adminId;
}
