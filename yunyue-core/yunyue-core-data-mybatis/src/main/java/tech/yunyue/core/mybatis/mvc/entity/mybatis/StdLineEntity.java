package tech.yunyue.core.mybatis.mvc.entity.mybatis;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import tech.yunyue.core.mvc.entity.Entity;

/**
 * 子表实体类基类，用于存放公共属性
 */
@Data
public abstract class StdLineEntity implements Entity {
    @TableId(type = IdType.ASSIGN_UUID)
    @TableField(jdbcType = JdbcType.VARCHAR)
    private String id;

    @NotBlank(message = "主表ID不允许为空")
    @Schema(description = "主表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String mainId;

    @Schema(description = "租户id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String tenantId;
}
