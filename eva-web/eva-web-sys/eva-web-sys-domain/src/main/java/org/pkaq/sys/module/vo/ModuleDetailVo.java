package org.pkaq.sys.module.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdTreeVo;

import java.util.List;

/**
 * 模块管理
 *
 * @author : PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "模块管理详情Vo")
public class ModuleDetailVo extends StdTreeVo {

    @Schema(description = "模块图标")
    private String icon;

    @Schema(description = "模块路由 URL")
    private String routeUrl;

    @Schema(description = "模块前端组件 URL")
    private String componentUrl;

    @Schema(description = "模块拥有的资源")
    private List<ModuleResourcesVo> resources;

}
