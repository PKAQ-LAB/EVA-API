package org.pkaq.sys.user.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.Bo;

/**
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "重置密码Bo")
public class UserCheckBo implements Bo {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "编号")
    @NotBlank(message = "{sys.user.code.required}")
    private String code;

    @Schema(description = "账号")
    @NotBlank(message = "{sys.user.account.required}")
    private String account;
}
