package org.pkaq.sys.organization.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.vo.StdTreeVo;

/**
 * 组织管理列表视图对象（树形）
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "组织管理列表Vo")
public class OrganizationListVo extends StdTreeVo {

    @Schema(description = "上级节点名称")
    private String parentName;

    /** TreeSelect 组件需要的 key */
    @Schema(description = "key")
    public Long getKey() {
        return this.getId();
    }

    /** TreeSelect 组件按 title 显示节点名*/
    @Schema(description = "title")
    public String getTitle() {
        return this.getName();
    }
}
