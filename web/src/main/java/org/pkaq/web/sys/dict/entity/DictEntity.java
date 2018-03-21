package org.pkaq.web.sys.dict.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.BaseEntity;
import org.pkaq.web.sys.organization.entity.OrganizationEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 字典项管理实体类
 * @author S.PKAQ
 */
@Data
@Alias("dict")
@TableName("sys_dict")
@EqualsAndHashCode(callSuper = true)
public class DictEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;
    /**  编码 **/
    private String code;
    /**  编码描述 **/
    private String name;
    /** 上级节点 **/
    private String parentid;
    /** 状态,0 已删除,1 可用 **/
    private String status;

    @TableField(exist = false)
    private List<DictEntity> children;

    public List<DictEntity> getChildren() {
        return children == null || children.size()<1?null:children;
    }
}