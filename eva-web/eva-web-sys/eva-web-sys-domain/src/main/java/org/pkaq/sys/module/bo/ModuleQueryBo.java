package org.pkaq.sys.module.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.Bo;

/**
 * 模块管理
 *
 * @author : PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "模块管理詳情Bo")
public class ModuleQueryBo implements Bo {
    @Schema(description = "编码")
    private String code;

    @Schema(description = "节点名称")
    private String name;

    @Schema(description = "上级节点id")
    private String pid;

}