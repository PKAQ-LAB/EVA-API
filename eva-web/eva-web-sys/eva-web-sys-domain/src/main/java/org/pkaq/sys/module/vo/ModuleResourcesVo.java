package org.pkaq.sys.module.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.entity.Entity;

/**
 * @author : PKAQ
 */
@Data
@Schema(title = "模块资源管理")
@EqualsAndHashCode(callSuper = false)
public class ModuleResourcesVo implements Entity {

    private Long id;

    @Schema(description = "模块id")
    private Long mainId;

    @Schema(description = "资源编码")
    private String code;

    @Schema(description = "璧勬簮鎻忚堪")
    private String resourceDesc;

    @Schema(description = "璧勬簮璺緞")
    private String resourceUrl;

    @Schema(description = "资源类型（按钮、操作）")
    private String resourceType;

    @Schema(description = "排序")
    private Double sort;

    @Schema(description = "是否已授权")
    private Integer checked;
}
