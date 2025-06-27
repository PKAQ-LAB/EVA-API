package org.pkaq.sys.role.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.vo.Vo;
import org.pkaq.sys.module.vo.ModuleDetailVo;

import java.util.Collection;
import java.util.Set;

/**
 * @author PKAQ
 */
@Data
@Schema(title = "角色持有的资源VO")
public class RoleGrantedModuleVo implements Vo {
    @Schema(description = "模块/资源集合")
    private Collection<ModuleDetailVo> modules;

    @Schema(description = "选中的模块ID集合")
    private Set<Long> checkedModuleIds;
}
