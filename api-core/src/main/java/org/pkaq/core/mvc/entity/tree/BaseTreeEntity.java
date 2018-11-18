package org.pkaq.core.mvc.entity.tree;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.pkaq.core.mvc.entity.PureBaseEntity;

import java.util.List;

/**
 * 树形结构实体积累
 * @author: S.PKAQ
 * @Datetime: 2018/11/18 13:20
 */
@Data
public class BaseTreeEntity extends PureBaseEntity {
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

    @TableField(exist = false)
    @ApiModelProperty("子节点")
    private List<BaseTreeEntity> children;

    @ApiModelProperty("key")
    @TableField(exist = false)
    private String key;

    @ApiModelProperty("exact")
    @TableField(exist = false)
    private boolean exact;

    public String getKey(){
        return this.getId();
    }

    public boolean getExact(){
        return this.isleaf;
    }

}
