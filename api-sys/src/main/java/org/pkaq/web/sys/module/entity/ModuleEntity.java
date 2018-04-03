package org.pkaq.web.sys.module.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.entity.BaseEntity;
import org.pkaq.web.sys.dict.entity.DictEntity;

import java.io.Serializable;
import java.util.List;

/**
 * 模块管理
 * @author : PKAQ
 */
@Data
@Alias("module")
@TableName("sys_module")
@EqualsAndHashCode(callSuper = true)
public class ModuleEntity extends BaseEntity implements Serializable {
    private String name;

    private String icon;

    private String routeurl;

    private String modelurl;

    @TableField("parentId")
    private String parentId;

    @TableField("parentName")
    private String parentName;

    private String path;

    @TableField("idPath")
    private String idPath;

    @TableField("namePath")
    private String namePath;

    private boolean isleaf;

    private Integer orders;

    private String status;

    private static final long serialVersionUID = 1L;

    /** 子节点 **/
    @TableField(exist = false)
    private List<DictEntity> children;

    public List<DictEntity> getChildren() {
        return children == null || children.size()<1?null:children;
    }

}