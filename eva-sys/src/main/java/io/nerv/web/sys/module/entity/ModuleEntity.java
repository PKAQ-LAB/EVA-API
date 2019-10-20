package io.nerv.web.sys.module.entity;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.nerv.core.mvc.entity.mybatis.BaseTreeEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
@ApiModel("模块管理")
public class ModuleEntity extends BaseTreeEntity {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty("模块名称")
    @TableField(condition = SqlCondition.LIKE_RIGHT)
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

    @TableField(exist = false)
    @ApiModelProperty("模块拥有的资源")
    private List<ModuleResources> resources;

    public String getKey() {
        return "";
    }

}