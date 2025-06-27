package org.pkaq.sys.role.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.bo.Bo;

import java.util.List;

/**
 * @author PKAQ
 */
@Data
@Schema(title = "角色-用户关系保存")
public class RoleUserRefBo implements Bo {
    @Schema(description = "角色id")
    private Long roleId;

    @Schema(description = "用户ID列表")
    private List<Long> userId;
}
