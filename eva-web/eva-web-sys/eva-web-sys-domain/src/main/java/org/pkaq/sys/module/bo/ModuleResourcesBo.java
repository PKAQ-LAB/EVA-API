package org.pkaq.sys.module.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.Vo;

/**
 * @author PKAQ
 */
@Data
@Schema(title = "模块资源管理")
@EqualsAndHashCode(callSuper = false)
public class ModuleResourcesBo implements Vo {

    private Long id;

    @Schema(description = "模块id")
    private Long mainId;

    @Schema(description = "资源描述")
    private String resourceDesc;

    @Schema(description = "资源路径")
    private String resourceUrl;

    @Schema(description = "资源类型（按钮、操作）")
    private String resourceType;
}
