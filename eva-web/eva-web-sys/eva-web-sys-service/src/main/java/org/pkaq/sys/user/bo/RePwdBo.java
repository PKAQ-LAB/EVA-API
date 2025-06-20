package org.pkaq.sys.user.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.Bo;

/**
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "重置密码Bo")
public class RePwdBo implements Bo {
    @Schema(description = "用户id")
    private String userId;

    @Schema(description = "原始密码")
    private String originpassword;

    @Schema(description = "新密码")
    private String newpassword;
}
