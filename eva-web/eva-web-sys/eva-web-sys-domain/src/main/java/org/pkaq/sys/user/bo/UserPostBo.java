package org.pkaq.sys.user.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.Bo;

import java.util.List;

/**
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "用户岗位Bo")
public class UserPostBo implements Bo {

    @Schema(description = "目标用户ID")
    @NotBlank(message = "{sys.user.grant.userid.required}")
    private String userId;

    @Schema(description = "岗位Id")
    @NotBlank(message = "{sys.user.grant.roleid.required}")
    private List<String> postIds;
}
