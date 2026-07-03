package org.pkaq.sys.dict.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.pkaq.core.mvc.bo.Bo;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典新增编辑参数
 *
 * @author PKAQ
 */
@Data
@Schema(title = "字典编辑BO")
public class DictAoeBo implements Bo {
    @Schema(description = "字典 ID")
    private Long id;

    @Schema(description = "乐观锁")
    private Integer revision;

    @Schema(description = "是否锁定：0-正常，1-已锁定，9999-只读")
    private Integer frozen;

    @Schema(description = "展示顺序")
    private double sort;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "字典类型编码，单级字典模式下默认等于 code")
    private String type;

    @NotBlank(message = "{sys.dict.code.empty}")
    @Schema(description = "字典编码")
    private String code;

    @NotBlank(message = "{sys.dict.name.empty}")
    @Schema(description = "字典名称")
    private String name;

    @Schema(description = "字典明细")
    private List<DictLineBo> lines = new ArrayList<>();
}
