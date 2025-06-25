package org.pkaq.sys.module.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.Entity;

/**
 * 模块管理
 *
 * @author : PKAQ
 */
@Data
@Alias("moduleResource")
@TableName("sys_module_resources")
@Schema(title = "模块资源管理")
@EqualsAndHashCode(callSuper = false)
public class ModuleResources implements Entity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    @Schema(description = "模块id")
    private Long moduleId;

    @Schema(description = "资源描述")
    private String resourceDesc;

    @Schema(description = "资源路径")
    private String resourceUrl;

    @Schema(description = "资源类型（按钮、操作）")
    private String resourceType;
}