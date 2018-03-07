package org.pkaq.web.organization.entity;

import com.baomidou.mybatisplus.annotations.TableField;
import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableLogic;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.mapper.SqlCondition;
import lombok.Data;
import org.apache.ibatis.type.Alias;

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
public class OrganizationEntity implements Serializable {
    @TableId
    private String id;
    //子节点
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

    private Byte isleaf;

    private Integer orders;

    private String status;

    private String remark;

    private Date gmtCreate;

    private Date gmtModify;

    private String createBy;

    private String modifyBy;

    private static final long serialVersionUID = 1L;
}
