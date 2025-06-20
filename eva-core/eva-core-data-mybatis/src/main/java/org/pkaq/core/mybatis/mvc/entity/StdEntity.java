package org.pkaq.core.mybatis.mvc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.pkaq.core.mvc.entity.Entity;

import java.time.LocalDateTime;

/**
 * 实体类基类，用于存放公共属性
 *
 * @author: S.PKAQ
 */
@Data
public abstract class StdEntity implements Entity {
    @TableId(type = IdType.ASSIGN_UUID)
    @TableField(jdbcType = JdbcType.VARCHAR)
    private String id;

    /** 乐观锁 **/
    @Version
    private int revision;

    /** 逻辑删除 (0-未删除、timestamp-删除) **/
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private long deleted;

    /** 是否冻结（0 - 未冻结，1 - 冻结， -1 - 不可编辑） **/
    private int frozen;

    /** 展示顺序 **/
    private double sort;

    /** 租户id **/
    private String tenantId;

    /** 创建人Id **/
    @TableField(fill = FieldFill.INSERT, jdbcType = JdbcType.VARCHAR)
    private String createId;

    /** 创建人 **/
    @TableField(fill = FieldFill.INSERT, jdbcType = JdbcType.VARCHAR)
    private String createBy;

    /** 创建时间 **/
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime utcCreate;

    /** 修改人Id **/
    @TableField(fill = FieldFill.INSERT_UPDATE, jdbcType = JdbcType.VARCHAR)
    private String modifyId;

    /** 修改人 **/
    @TableField(fill = FieldFill.INSERT_UPDATE, jdbcType = JdbcType.VARCHAR)
    private String modifyBy;

    /** 修改时间 **/
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime utcModify;

    /** 备注 **/
    private String remark;
}
