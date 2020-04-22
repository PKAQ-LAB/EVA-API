package io.nerv.web.sys.organization.entity;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import io.nerv.core.mvc.entity.mybatis.StdBaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.util.List;
/**
 * 组织管理实体类
 * @author: S.PKAQ
 * Datetime: 2018/3/2 13:50
 */

@Data
@Alias("organization")
@TableName("sys_organization")
@EqualsAndHashCode(callSuper = true)
@ApiModel("组织管理")
public class OrganizationEntity extends StdBaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField(condition = SqlCondition.LIKE)
    @ApiModelProperty("组织名称")
    private String name;

    @TableField(condition = SqlCondition.LIKE)
    @ApiModelProperty("编码")
    private String code;

    @ApiModelProperty("上级节点Id")
    private String parentId;

    @ApiModelProperty("上级节点名称")
    private String parentName;

    @ApiModelProperty("上级节点id路径")
    private String path;

    @ApiModelProperty("上级节点路径描述")
    private String pathName;

    @ApiModelProperty("是否是叶子")
    private boolean isleaf;

    @ApiModelProperty("排序")
    private Integer orders;

    @TableLogic
    @ApiModelProperty("逻辑删除状态")
    private String deleted;

    @ApiModelProperty("是否可用")
    private String status;

    @TableField(exist = false)
    @ApiModelProperty("子节点")
    private List<OrganizationEntity> children;

    /**
     * TreeSelect组件需要为一个key
     */
    @TableField(exist = false)
    @ApiModelProperty("key")
    private String key;

    /**
     * TreeSelect组件指定treeNodeLabelProp无法生效 仍然按默认title属性读取 这里添加title返回
     */
    @TableField(exist = false)
    @ApiModelProperty("title")
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
