package org.pkaq.core.mybatis.mvc.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.entity.Entity;
import org.pkaq.core.util.StrUtils;

import java.util.List;

/**
 * 树形结构实体基类
 *
 * @author: S.PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TreeEntity implements Entity {
    @Schema(description = "id")
    private Long id;

    @TableField(condition = SqlCondition.LIKE)
    @Schema(description = "编码")
    private String code;

    @Schema(description = "是否可用")
    private String status;

    @Schema(description = "节点名称")
    private String name;

    @Schema(description = "上级节点id")
    private String parentId;

    @Schema(description = "上级节点名称")
    private String parentName;

    @Schema(description = "排序")
    private long orders;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "上级节点id路径")
    private String pathId;

    @Schema(description = "上级节点名称路径")
    private String pathName;

    @Schema(description = "是否叶子")
    private Boolean isleaf;

    @TableField(exist = false)
    @Schema(description = "子节点")
    private List<TreeEntity> children;

    @Schema(description = "key")
    @TableField(exist = false)
    private String key;

    @Schema(description = "exact")
    @TableField(exist = false)
    private Boolean exact;

    @TableField(exist = false)
    @Schema(description = "国际化面包屑")
    private String locale;

    public String getLocale() {
        return StrUtils.isNotBlank(this.path) ? "menu" + this.path.replaceAll("/", ".") : "";
    }

    public Long getKey() {
        return this.getId();
    }

    public Boolean getExact() {
        return this.isleaf;
    }

    public List<TreeEntity> getChildren() {
        return children == null || children.size() < 1 ? null : children;
    }

    public List<TreeEntity> getOriginChildren() {
        return children;
    }
}
