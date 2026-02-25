package org.pkaq.sys.dict.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.vo.Vo;

/**
 * 字典视图
 *
 * @author PKAQ
 */
@Data
public class DictViewVo implements Vo {
    @Schema(description = "字典编码")
    private String code;

    @Schema(description = "字典描述")
    private String name;

    @Schema(description = "字典项key")
    private String dCode;

    @Schema(description = "字典项value")
    private String dValue;

    @Schema(description = "是否冻结（0000 - 未冻结，0001 - 冻结， 9999 - 不可编辑）")
    private String frozen;

    @Schema(description = "展示顺序")
    private int sort;
}
