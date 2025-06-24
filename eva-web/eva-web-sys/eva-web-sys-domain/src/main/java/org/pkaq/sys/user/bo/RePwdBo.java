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
@Schema(title = "用户创建Bo")
public class RePwdBo implements Bo {
    private int revision;

    @Schema(description = "原始密码")
    @NotBlank(message = "{sys.user.oldpwd.required}")
    private String originPassword;

    @Schema(description = "新密码")
    @NotBlank(message = "{sys.user.newpwd.required}")
    private String newPassword;
}
