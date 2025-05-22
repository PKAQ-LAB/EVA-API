package org.pkaq.sys.tenant.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 租户管理-详情-请求参数
 *
 * @author PKAQ
 */
@Schema(description = "租户管理-详情-请求参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantAoeBo {
    private String id;

    @Schema(description = "租户名称")
    @Size(max = 40)
    @NotBlank
    private String name;

    @Schema(description = "租户编码")
    @Size(max = 6)
    @NotBlank
    private String code;

    @Schema(description = "租户类型")
    @Size(max = 6)
    @NotBlank
    private String type;

    @Schema(description = "全称")
    @Size(max = 60)
    @NotBlank
    private String fullName;

    @Schema(description = "证件类型")
    @Size(max = 6)
    @NotBlank
    private String cardType;

    @Schema(description = "证件号")
    @Size(max = 30)
    @NotBlank
    private String idCard;

    @Schema(description = "联系人")
    @Size(max = 40)
    @NotBlank
    private String contactName;

    @Schema(description = "联系方式")
    @Size(max = 16)
    @NotBlank
    private String contactTel;

    @Schema(description = "授权用户数")
    private int authUserCount;

    @Schema(description = "到期时间")
    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date expirationDate;

    @Schema(description = "租户管理员账号")
    @NotNull
    private String adminAccount;

    @Schema(description = "租户管理员密码")
    private String adminPass;


    @Schema(description = "备注")
    private String remark;
}
