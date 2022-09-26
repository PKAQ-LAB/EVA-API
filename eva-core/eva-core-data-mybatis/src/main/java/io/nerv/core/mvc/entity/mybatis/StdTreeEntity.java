package io.nerv.core.mvc.entity.mybatis;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.annotation.SqlCondition;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 树形结构实体基类
 * @author: S.PKAQ
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class StdTreeEntity extends StdEntity {
    @TableField(condition = SqlCondition.LIKE)
    @Schema(description = "编码")
    private String code;

    @Schema(description = "是否可用")
    private String status;

    @Schema(description ="节点名称")
    private String name;

    @Schema(description ="上级节点id")
    private String parentId;

    @Schema(description ="上级节点名称")
    private String parentName;

    @Schema(description ="路径")
    private long orders;
    @Schema(description ="路径")
    private String path;

    @Schema(description ="上级节点id路径")
    private String pathId;

    @Schema(description ="上级节点名称路径")
    private String pathName;

    @Schema(description ="是否叶子")
    private Boolean isleaf;

    @TableField(exist = false)
    @Schema(description ="子节点")
    private List<StdTreeEntity> children;

    @Schema(description ="key")
    @TableField(exist = false)
    private String key;

    @Schema(description ="exact")
    @TableField(exist = false)
    private Boolean exact;

    @TableField(exist = false)
    @Schema(description ="国际化面包屑")
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

    public List<StdTreeEntity> getChildren(){
        return children == null || children.size()<1?null:children;
    }

    public List<StdTreeEntity> getOriginChildren(){
        return children;
    }
}
