package org.pkaq.sys.tenant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;
import org.apache.ibatis.type.JdbcType;
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
@Schema(title = "租户管理")
@EqualsAndHashCode(callSuper = true)
public class TenantEntity extends StdEntity {
    @Schema(description = "租户名称")
    private String name;

    @Schema(description = "租户编码")
    private String code;

    @Schema(description = "租户类型")
    private String type;

    @Schema(description = "全称")
    private String fullName;

    @Schema(description = "证件类型")
    private String cardType;

    @Schema(description = "证件号")
    private String cardNo;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系方式")
    private String contactTel;

    @Schema(description = "授权用户数")
    private int authUserCount;

    @Schema(description = "到期时间")
    private Date expirationDate;

    @Schema(description = "管理员Id")
    private String adminId;
}
