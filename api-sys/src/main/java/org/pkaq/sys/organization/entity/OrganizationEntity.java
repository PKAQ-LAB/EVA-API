package org.pkaq.sys.organization.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.mapper.SqlCondition;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.BaseEntity;

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
public class OrganizationEntity extends BaseEntity<String>{
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
    @ApiModelProperty("状态")
    private String status;
    @TableField(exist = false)
    @ApiModelProperty("子节点")
    private List<OrganizationEntity> children;

    public List<OrganizationEntity> getChildren() {
        return children == null || children.size()<1?null:children;
    }

}
