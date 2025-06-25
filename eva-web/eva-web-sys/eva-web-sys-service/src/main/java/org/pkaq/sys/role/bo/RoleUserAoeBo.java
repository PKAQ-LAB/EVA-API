package org.pkaq.sys.role.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.pkaq.core.mvc.bo.Bo;
import org.pkaq.sys.role.entity.RoleUserEntity;

import java.util.List;

/**
 * @author PKAQ
 */
@Data
@Schema(title = "角色-用户关系保存")
public class RoleUserAoeBo implements Bo {
    @Schema(description = "角色id")
    private Long roleId;

    @Schema(description = "用户ID列表")
    @NotNull
    private List<RoleUserEntity> users;
}
