package org.pkaq.core.mybatis.mvc.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.pkaq.core.mvc.entity.Entity;
import org.springframework.format.annotation.DateTimeFormat;

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

    @Schema(description = "乐观锁")
    @Version
    private int revision;

    @Schema(description = "逻辑删除 (0-未删除、timestamp-删除)")
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private long deleted;

    @Schema(description = "是否冻结（0000 - 未冻结，0001 - 冻结， 9999 - 不可编辑）")
    private String frozen;

    @Schema(description = "展示顺序")
    private int sort;

    @Schema(description = "租户id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String tenantId;

    @Schema(description = "创建人Id")
    @TableField(fill = FieldFill.INSERT, jdbcType = JdbcType.VARCHAR)
    private String createId;

    @Schema(description = "创建人")
    @TableField(fill = FieldFill.INSERT, jdbcType = JdbcType.VARCHAR)
    private String createBy;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime utcCreate;

    @Schema(description = "修改人Id")
    @TableField(fill = FieldFill.INSERT_UPDATE, jdbcType = JdbcType.VARCHAR)
    private String modifyId;

    @Schema(description = "修改人")
    @TableField(fill = FieldFill.INSERT_UPDATE, jdbcType = JdbcType.VARCHAR)
    private String modifyBy;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime utcModify;

    @Schema(description = "备注")
    private String remark;
}
