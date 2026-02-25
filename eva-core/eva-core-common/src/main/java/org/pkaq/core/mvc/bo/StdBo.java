package org.pkaq.core.mvc.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

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

    /**
     * 是否冻结（0 - 未冻结，1 - 冻结， -1 - 不可编辑）
     **/
    private Integer frozen;

    @Schema(description = "展示顺序")
    private double sort;

    @Schema(description = "备注")
    @Length(max = 400, message = "{validate.length.max}")
    private String remark;
}
