package org.pkaq.core.mvc.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 实体类基类，用于存放公共属性
 *
 * @author PKAQ
 */
@Data
public abstract class StdBo implements Bo {
    private Long id;

    @Schema(description = "编码")
    private String code;

    @Schema(description = "乐观锁")
    private Integer revision;

    @Schema(description = "是否锁定：0-正常，1-已锁定，9999-只读")
    private Integer frozen;

    @Schema(description = "展示顺序")
    private double sort;

    @Schema(description = "备注")
    @Size(max = 400, message = "{validate.length.max}")
    private String remark;
}
