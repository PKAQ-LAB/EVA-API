package io.nerv.sys.web.organization.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.nerv.common.mvc.entity.mybatis.StdEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.util.List;

/**
 * 组织管理实体类
 * @author: S.PKAQ
 */

@Data
@Alias("organization")
@TableName("sys_organization")
@EqualsAndHashCode(callSuper = true)
@Schema(title = "组织管理")
public class OrganizationEntity extends StdEntity {
    private static final long serialVersionUID = 1L;

    @TableField(condition = SqlCondition.LIKE)
    @Schema(description = "组织名称")
    private String name;

    @TableField(condition = SqlCondition.LIKE)
    @Schema(description = "编码")
    private String code;

    @Schema(description = "上级节点Id")
    private String parentId;

    @Schema(description = "上级节点名称")
    private String parentName;

    @Schema(description = "上级节点id路径")
    private String path;

    @Schema(description = "上级节点路径描述")
    private String pathName;

    @Schema(description = "是否是叶子")
    private boolean isleaf;

    @Schema(description = "排序")
    private Integer orders;

    @TableLogic
    @Schema(description = "逻辑删除状态")
    private String deleted;

    @Schema(description = "是否可用")
    private String status;

    @TableField(exist = false)
    @Schema(description = "子节点")
    private List<OrganizationEntity> children;

    /**
     * TreeSelect组件需要为一个key
     */
    @TableField(exist = false)
    @Schema(description = "key")
    private String key;

    /**
     * TreeSelect组件指定treeNodeLabelProp无法生效 仍然按默认title属性读取 这里添加title返回
     */
    @TableField(exist = false)
    @Schema(description = "title")
    private String title;

    public String getKey() {
        return this.getId();
    }

    public String getTitle() {
        return this.name;
    }

    public String getValue() {
        return this.getId();
    }

    public List<OrganizationEntity> getChildren() {
        return children == null || children.size()<1?null:children;
    }

}
