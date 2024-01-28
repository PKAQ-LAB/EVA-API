package tech.yunyue.core.mybatis.mvc.entity.mybatis;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import tech.yunyue.core.mvc.entity.Entity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 实体类基类，用于存放公共属性
 *
 * @author: S.PKAQ
 */
@Data
public abstract class StdEntity implements Entity {
    @TableId(type = IdType.ASSIGN_ID)
    @TableField(jdbcType = JdbcType.VARCHAR)
    private String id;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "乐观锁")
    @Version
    private Integer revision;

    @Schema(description = "逻辑删除 (null-未删除、不为null-删除)")
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime deleted;

    @Schema(description = "租户id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String tenantId;

    @Schema(description = "创建人岗位ID")
    @TableField(fill = FieldFill.INSERT, jdbcType = JdbcType.VARCHAR)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String postId;

    @Schema(description = "创建人部门ID")
    @TableField(fill = FieldFill.INSERT, jdbcType = JdbcType.VARCHAR)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String orgId;

    @Schema(description = "创建人ID")
    @TableField(fill = FieldFill.INSERT, jdbcType = JdbcType.VARCHAR)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String createId;

    @Schema(description = "创建人")
    @TableField(fill = FieldFill.INSERT, jdbcType = JdbcType.VARCHAR)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String createBy;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime gmtCreate;

    @Schema(description = "修改人ID")
    @TableField(fill = FieldFill.INSERT_UPDATE, jdbcType = JdbcType.VARCHAR)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String modifyId;

    @Schema(description = "修改人")
    @TableField(fill = FieldFill.INSERT_UPDATE, jdbcType = JdbcType.VARCHAR)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String modifyBy;

    @Schema(description = "修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime gmtModify;
}
