package org.pkaq.web.organization.entity;

import com.baomidou.mybatisplus.activerecord.Model;
import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.mapper.SqlCondition;
import lombok.Data;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mvc.BaseEntity;

import java.io.Serializable;
import java.sql.Date;
import java.util.List;
/**
 * 组织管理实体类
 * @author: S.PKAQ
 * Datetime: 2018/3/2 13:50
 */
@Data
@Alias("orginization")
@TableName("sys_orginization")
public class OrganizationEntity extends BaseEntity{
    private static final long serialVersionUID = 1L;

    @TableField(exist = false)
    private List<OrganizationEntity> children;

    @TableField(condition = SqlCondition.LIKE)
    private String name;

    @TableField(condition = SqlCondition.LIKE)
    private String code;

    private String parentid;

    private String parentname;

    private String path;

    private String pathname;

    private boolean isleaf;

    private Integer orders;

    private String status;

    public List<OrganizationEntity> getChildren() {
        return children == null || children.size()<1?null:children;
    }

}
