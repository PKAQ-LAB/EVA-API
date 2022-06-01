package io.nerv.web.sys.module.entity;

import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.nerv.core.mvc.entity.mybatis.BaseTreeEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;

import java.util.List;

/**
 * 模块管理
 * @author : PKAQ
 */
@Data
@Alias("module")
@TableName("sys_module")
@EqualsAndHashCode(callSuper = true)
@Schema(title = "模块管理")
public class ModuleEntity extends BaseTreeEntity {
    private static final long serialVersionUID = 1L;

    @Schema(name = "模块名称")
    @TableField(condition = SqlCondition.LIKE_RIGHT)
    private String name;

    @Schema(name = "模块图标")
    private String icon;

    @Schema(name = "模块路由url")
    private String routeurl;

    @Schema(name = "模块前端model url")
    private String modelurl;

    @Schema(name = "排序")
    private Integer orders;

    @Schema(name = "状态")
    private String status;

    @TableField(exist = false)
    @Schema(name = "模块拥有的资源")
    private List<ModuleResources> resources;

    public String getKey() {
        return "";
    }

}