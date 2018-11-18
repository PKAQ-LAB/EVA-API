package org.pkaq.web.sys.module.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.tree.BaseTreeEntity;

/**
 * 模块管理
 * @author : PKAQ
 */
@Data
@Alias("module")
@TableName("sys_module")
@EqualsAndHashCode(callSuper = true)
@ApiModel("模块管理")
public class ModuleEntity extends BaseTreeEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("模块名称")
    private String name;

    @ApiModelProperty("模块图标")
    private String icon;

    @ApiModelProperty("模块路由url")
    private String routeurl;

    @ApiModelProperty("模块前端model url")
    private String modelurl;

    @ApiModelProperty("排序")
    private Integer orders;

    @ApiModelProperty("状态")
    private String status;
}