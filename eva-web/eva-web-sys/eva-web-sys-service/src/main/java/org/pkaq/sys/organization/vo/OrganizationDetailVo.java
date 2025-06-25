package org.pkaq.sys.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdVo;

import java.util.List;

/**
 * 组织管理实体类
 *
 * @author: S.PKAQ
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(title = "组织管理列表vo")
public class OrganizationDetailVo extends StdVo {

    @Schema(description = "组织名称")
    private String name;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "上级节点Id")
    private String pid;

    @Schema(description = "上级节点id路径")
    private String path;

    @Schema(description = "是否是叶子")
    private boolean isleaf;

    @Schema(description = "子节点")
    private List<OrganizationDetailVo> children;


    /**
     * TreeSelect组件指定treeNodeLabelProp无法生效 仍然按默认title属性读取 这里添加title返回
     */
    @Schema(description = "title")
    private String title;

    public Long getKey() {
        return this.getId();
    }

    public String getTitle() {
        return this.name;
    }

    public Long getValue() {
        return this.getId();
    }

    public List<OrganizationDetailVo> getChildren() {
        return children == null || children.size() < 1 ? null : children;
    }

}
