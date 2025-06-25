package org.pkaq.core.mybatis.mvc.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.entity.Entity;


/**
 * 实体类基类，用于存放公共属性
 *
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper = false)
public abstract class StdMultiLineEntity implements Entity {

    @NotBlank(message = "主表ID不允许为空")
    @Schema(description = "主表ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long mainId;
}
