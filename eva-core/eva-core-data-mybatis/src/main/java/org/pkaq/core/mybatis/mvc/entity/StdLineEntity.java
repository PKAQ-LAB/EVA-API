package org.pkaq.core.mybatis.mvc.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.JdbcType;
import org.pkaq.core.mvc.entity.Entity;

import java.time.LocalDateTime;


/**
 * 实体类基类，用于存放公共属性
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class StdLineEntity implements Entity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long mainId;

    /**
     * 租户id
     **/
    @TableField(fill = FieldFill.INSERT)
    private Long tenantId;

    /**
     * 创建人Id
     **/
    @TableField(fill = FieldFill.INSERT)
    private Long createId;

    /**
     * 创建人
     **/
    @TableField(fill = FieldFill.INSERT, jdbcType = JdbcType.VARCHAR)
    private String createBy;

    /**
     * 创建时间
     **/
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime utcCreate;

    /**
     * 修改人Id
     **/
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long modifyId;

    /**
     * 修改人
     **/
    @TableField(fill = FieldFill.INSERT_UPDATE, jdbcType = JdbcType.VARCHAR)
    private String modifyBy;

    /**
     * 修改时间
     **/
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime utcModify;
}
