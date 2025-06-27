package org.pkaq.sys.module.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdTreeVo;

/**
 * 模块管理
 *
 * @author : PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "模块管理列表Vo")
public class ModuleListVo extends StdTreeVo {

    @Schema(description = "模块图标")
    private String icon;

    @Schema(description = "模块路由url")
    private String routeurl;

    @Schema(description = "模块前端model url")
    private String modelurl;
}