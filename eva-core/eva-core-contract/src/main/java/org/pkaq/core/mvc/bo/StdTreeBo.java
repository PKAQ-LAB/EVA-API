package org.pkaq.core.mvc.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 树形结构实体基类
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class StdTreeBo extends StdBo {

    @Schema(description = "编码")
    private String code;

    @Schema(description = "节点名称")
    private String name;

    @Schema(description = "上级节点id")
    private Long pid;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "是否叶子")
    private Boolean isleaf;
}
