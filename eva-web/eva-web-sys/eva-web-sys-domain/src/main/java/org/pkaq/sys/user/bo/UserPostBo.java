package org.pkaq.sys.user.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.Bo;

import java.util.List;

/**
 * 用户岗位授权参数
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "用户岗位Bo")
public class UserPostBo implements Bo {

    @Schema(description = "目标用户ID")
    @NotNull(message = "{sys.user.grant.userid.required}")
    private Long userId;

    @Schema(description = "岗位ID")
    @NotNull(message = "{sys.user.grant.roleid.required}")
    private List<Long> postIds;
}
