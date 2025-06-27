package org.pkaq.sys.module.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.StdTreeBo;

import java.util.List;

/**
 * 模块管理
 *
 * @author : PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "模块管理詳情Bo")
public class ModuleAoeBo extends StdTreeBo {

    @Schema(description = "模块图标")
    private String icon;

    @Schema(description = "模块路由url")
    private String routeurl;

    @Schema(description = "模块前端model url")
    private String modelurl;

    @Schema(description = "模块拥有的资源")
    private List<ModuleResourcesBo> resources;

}