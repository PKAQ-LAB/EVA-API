package org.pkaq.sys.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.vo.Vo;
import org.pkaq.sys.module.vo.ModuleResourcesVo;

import java.util.List;

/**
 * @author : PKAQ
 */
@Data
@Schema(title = "用户拥有的模块")
public class UserResourceVo implements Vo {

    private Long id;

    private Long pid;

    @Schema(description = "模块路由url")
    private String routeurl;

    @Schema(description = "模块前端model url")
    private String modelurl;

    @Schema(description = "子节点")
    private List<UserResourceVo> children;

    @Schema(description = "模块拥有的资源")
    private List<ModuleResourcesVo> resources;

}