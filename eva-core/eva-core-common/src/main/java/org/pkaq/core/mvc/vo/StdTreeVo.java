package org.pkaq.core.mvc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 树形结构实体基类
 *
 * @author: S.PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class StdTreeVo extends StdVo {

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

    @Schema(description = "是否选中")
    private boolean checked = false;

    @Schema(description = "子节点")
    private List<StdTreeVo> children;

    public Long getKey() {
        return this.getId();
    }

    public Boolean getExact() {
        return this.isleaf;
    }

    public List<StdTreeVo> getChildren() {
        return children == null || children.isEmpty() ? null : children;
    }

    public List<StdTreeVo> getOriginChildren() {
        return children;
    }
}
