package org.pkaq.sys.tenant.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.StdBo;

import java.util.Date;

/**
 * 租户管理-详情-请求参数
 *
 * @author PKAQ
 */
@Schema(description = "租户管理-详情-请求参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class TenantAoeBo extends StdBo {

    @Schema(description = "租户名称")
    @Size(max = 40, message = "{validate.length.max}")
    @NotBlank
    private String name;

    @Schema(description = "租户编码")
    @Size(max = 6, message = "{validate.length.max}")
    @NotBlank
    private String code;

    @Schema(description = "租户类型")
    @Size(max = 6, message = "{validate.length.max}")
    @NotBlank
    private String type;

    @Schema(description = "全称")
    @Size(max = 60, message = "{validate.length.max}")
    @NotBlank
    private String fullName;

    @Schema(description = "证件类型")
    @Size(max = 6, message = "{validate.length.max}")
    @NotBlank
    private String cardType;

    @Schema(description = "证件号")
    @Size(max = 30, message = "{validate.length.max}")
    @NotBlank
    private String idCard;

    @Schema(description = "联系人")
    @Size(max = 40, message = "{validate.length.max}")
    @NotBlank
    private String contactName;

    @Schema(description = "联系方式")
    @Size(max = 16, message = "{validate.length.max}")
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
}
