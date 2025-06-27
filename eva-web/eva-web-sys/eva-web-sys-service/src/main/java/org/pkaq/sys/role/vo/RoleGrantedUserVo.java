package org.pkaq.sys.role.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.vo.Vo;
import org.pkaq.sys.user.vo.UserSimpleVo;

import java.util.List;
import java.util.Set;

/**
 * @author PKAQ
 */
@Data
@Schema(title = "角色持有的资源VO")
public class RoleGrantedUserVo implements Vo {
    @Schema(description = "用户集合")
    private List<UserSimpleVo> users;

    @Schema(description = "选中的用户集合")
    private Set<Long> checkedUser;
}
