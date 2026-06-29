package org.pkaq.sys.module.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 前端菜单资源视图。
 *
 * @author PKAQ
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(title = "前端菜单资源视图")
public class ModuleMenuResourceVo {

    @Schema(description = "资源ID")
    private Long id;

    @Schema(description = "资源编码")
    private String code;

    @Schema(description = "资源描述")
    private String resourceDesc;

    @Schema(description = "资源路径")
    private String resourceUrl;

    @Schema(description = "资源类型")
    private String resourceType;
}
