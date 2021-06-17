package io.nerv.core.mvc.entity.mybatis;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 树形结构实体基类
 * @author: S.PKAQ
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class BaseTreeEntity extends StdBaseEntity {
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
    private Boolean isleaf;

    @TableField(exist = false)
    @ApiModelProperty("子节点")
    private List<BaseTreeEntity> children;

    @ApiModelProperty("key")
    @TableField(exist = false)
    private String key;

    @ApiModelProperty("exact")
    @TableField(exist = false)
    private Boolean exact;

    @TableField(exist = false)
    @ApiModelProperty("国际化面包屑")
    private String locale;

    public String getLocale() {
        return StrUtil.isNotBlank(this.path) ? "menu"+this.path.replaceAll("/","."): "";
    }

    public String getKey(){
        return this.getId();
    }

    public Boolean getExact(){
        return this.isleaf;
    }

    public List<BaseTreeEntity> getChildren(){
        return children == null || children.size()<1?null:children;
    }

    public List<BaseTreeEntity> getOriginChildren(){
        return children;
    }

}
