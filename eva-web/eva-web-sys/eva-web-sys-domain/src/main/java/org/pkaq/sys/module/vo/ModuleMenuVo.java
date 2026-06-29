package org.pkaq.sys.module.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 前端菜单视图。
 *
 * @author PKAQ
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(title = "前端菜单视图")
public class ModuleMenuVo {

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "菜单编码")
    private String code;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "父菜单ID")
    private Long pid;

    @Schema(description = "菜单树路径")
    private String path;

    @Schema(description = "是否叶子节点")
    private Boolean isleaf;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "菜单路由URL")
    private String routeUrl;

    @Schema(description = "菜单组件URL")
    private String componentUrl;

    @Schema(description = "展示顺序")
    private Double sort;

    @Schema(description = "菜单资源")
    private List<ModuleMenuResourceVo> resources;

    @Schema(description = "子菜单")
    private List<ModuleMenuVo> children;

    public Long getKey() {
        return this.id;
    }

    public Boolean getExact() {
        return this.isleaf;
    }
}
