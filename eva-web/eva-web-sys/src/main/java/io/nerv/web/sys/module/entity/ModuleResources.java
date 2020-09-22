package io.nerv.web.sys.module.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.nerv.core.mvc.entity.mybatis.Entity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.ibatis.type.Alias;

/**
 * 模块管理
 * @author : PKAQ
 */
@Data
@Alias("moduleResource")
@TableName("sys_module_resources")
@ApiModel("模块资源管理")
public class ModuleResources implements Entity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @ApiModelProperty("模块id")
    private String moduleId;

    @ApiModelProperty("资源描述")
    private String resourceDesc;

    @ApiModelProperty("资源路径")
    private String resourceUrl;

    @ApiModelProperty("资源类型（按钮、操作）")
    private String resourceType;
}