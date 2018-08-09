package org.pkaq.sys.module.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.BaseEntity;

import java.io.Serializable;
import java.util.List;

/**
 * 模块管理
 * @author : PKAQ
 */
@Data
@Alias("module")
@TableName("sys_module")
@EqualsAndHashCode(callSuper = true)
@ApiModel("模块管理")
public class ModuleEntity extends BaseEntity<String> implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("模块名称")
    private String name;
    @ApiModelProperty("模块图标")
    private String icon;
    @ApiModelProperty("模块路由url")
    private String routeurl;
    @ApiModelProperty("模块前端model url")
    private String modelurl;
    @ApiModelProperty("上级节点id")
    private String parentId;
    @ApiModelProperty("上级节点名称")
    private String parentName;
    @ApiModelProperty("路径")
    private String path;
    @ApiModelProperty("上级节点id路径")
    private String pathId;
    @ApiModelProperty("上级节点名称路径")
    private String pathName;
    @ApiModelProperty("是否叶子")
    private boolean isleaf;
    @ApiModelProperty("排序")
    private Integer orders;
    @ApiModelProperty("状态")
    private String status;
    @TableField(exist = false)
    @ApiModelProperty("子节点")
    private List<ModuleEntity> children;
}