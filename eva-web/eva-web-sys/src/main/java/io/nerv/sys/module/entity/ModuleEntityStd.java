package io.nerv.sys.module.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.nerv.core.mybatis.mvc.entity.mybatis.StdTreeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.util.List;

/**
 * 模块管理
 *
 * @author : PKAQ
 */
@Data
@Alias("module")
@TableName("sys_module")
@EqualsAndHashCode(callSuper = true)
@Schema(title = "模块管理")
public class ModuleEntityStd extends StdTreeEntity {
    private static final long serialVersionUID = 1L;


    @Schema(description = "模块图标")
    private String icon;

    @Schema(description = "模块路由url")
    private String routeurl;

    @Schema(description = "模块前端model url")
    private String modelurl;

    @TableField(exist = false)
    @Schema(description = "模块拥有的资源")
    private List<ModuleResources> resources;

}