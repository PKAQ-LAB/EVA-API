package org.pkaq.sys.tenant.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.ibatis.type.Alias;
import org.apache.ibatis.type.JdbcType;
import org.pkaq.core.mvc.entity.Entity;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 租户管理
 *
 * @author 茂茂AdamEve
 */
@Data
@Alias("tenantEntity")
@TableName("sys_tenant")
@Schema(title = "租户管理")
@Accessors(chain = true)
public class TenantEntity implements Entity {
    @TableId(type = IdType.ASSIGN_ID)
    @TableField(jdbcType = JdbcType.VARCHAR)
    private String id;

    @Schema(description = "租户名称")
    private String name;

    @Schema(description = "租户编码")
    private String code;

    @Schema(description = "租户类型")
    private String type;

    @Schema(description = "全称")
    private String fullName;

    @Schema(description = "证件类型")
    private String cardType;

    @Schema(description = "证件号")
    private String idCard;

    @Schema(description = "联系人")
    private String contactName;

    @Schema(description = "联系方式")
    private String contactTel;

    @Schema(description = "授权用户数")
    private int authUserCount;

    @Schema(description = "到期时间")
    private Date expirationDate;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "管理员Id")
    private String adminId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "乐观锁")
    @Version
    private Integer revision;


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
