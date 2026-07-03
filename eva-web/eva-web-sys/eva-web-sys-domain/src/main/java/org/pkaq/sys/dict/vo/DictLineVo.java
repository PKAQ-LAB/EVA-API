package org.pkaq.sys.dict.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.vo.Vo;

/**
 * 字典明细视图对象
 *
 * @author PKAQ
 */
@Data
public class DictLineVo implements Vo {
    @Schema(description = "字典明细 ID")
    private Long id;

    @Schema(description = "字典提交值")
    private String keyName;

    @Schema(description = "字典显示文本")
    private String keyValue;

    @Schema(description = "排序")
    private Double orders;

    @Schema(description = "是否锁定：0-正常，1-已锁定，9999-只读")
    private Integer frozen;

    @Schema(description = "备注")
    private String remark;
}
