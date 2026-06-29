package org.pkaq.sys.user.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.Bo;

import java.util.List;

/**
 * 用户角色授权参数
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "用户授权Bo")
public class UserGrantBo implements Bo {

    @Schema(description = "目标用户ID")
    @NotNull(message = "{sys.user.grant.userid.required}")
    private Long userId;

    @Schema(description = "角色ID")
    @NotNull(message = "{sys.user.grant.roleid.required}")
    private List<Long> roleIds;
}
