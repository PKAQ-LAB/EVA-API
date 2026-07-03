package org.pkaq.core.mybatis.mvc.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.pkaq.core.enums.FrozenEnumm;
import org.pkaq.core.mvc.entity.Entity;

import java.time.LocalDateTime;

/**
 * 实体类基类，用于存放公共属性
 *
 * @author PKAQ
 */
@Data
public abstract class StdEntity implements Entity {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 乐观锁
     **/
    @Version
    @TableField("revision")
    private Integer revision;

    /**
     * 逻辑删除 (0-未删除、timestamp-删除)
     **/
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private long deleted;

    /**
     * 是否锁定（0 - 正常，1 - 已锁定，9999 - 只读）
     **/
    private FrozenEnumm frozen;

    /**
     * 展示顺序
     **/
    private Double sort;

    /**
     * 租户id（由 MybatisMetaObjectHandler 在新增时按 ThreadUser 自动填充，
     * 业务侧无需关心；显式 setTenantId 优先级更高）
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

    /**
     * 备注
     **/
    private String remark;
}
