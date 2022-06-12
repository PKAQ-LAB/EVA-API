package io.nerv.web.sys.module.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.nerv.core.mvc.entity.Entity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

/**
 * 模块管理
 * @author : PKAQ
 */
@Data
@Alias("moduleResource")
@TableName("sys_module_resources")
@Schema(title = "模块资源管理")
@EqualsAndHashCode(callSuper=false)
public class ModuleResources implements Entity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    @Schema(name = "模块id")
    private String moduleId;

    @Schema(name = "资源描述")
    private String resourceDesc;

    @Schema(name = "资源路径")
    private String resourceUrl;

    @Schema(name = "资源类型（按钮、操作）")
    private String resourceType;
}