package io.nerv.core.mvc.entity.mybatis;

import io.nerv.core.mvc.entity.Entity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;

/**
 * 实体类基类，用于存放公共属性
 * @author PKAQ
 */
@Data
@EqualsAndHashCode(callSuper=false)
public abstract class StdMultiLineEntity implements Entity {

    @NotBlank(message = "主表ID不允许为空")
    @Schema(description =  "主表ID", required = true)
    private String mainId;
}
