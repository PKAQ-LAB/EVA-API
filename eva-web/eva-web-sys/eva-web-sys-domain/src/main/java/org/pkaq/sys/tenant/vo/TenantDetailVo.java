package org.pkaq.sys.tenant.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdVo;

import java.util.Date;

/**
 * 租户管理详情视图对象
 *
 * @author PKAQ
 */
@Schema(description = "租户管理详情视图对象")
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantDetailVo extends StdVo {

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

    @Schema(description = "租户管理员账号")
    private String adminAccount;
}
