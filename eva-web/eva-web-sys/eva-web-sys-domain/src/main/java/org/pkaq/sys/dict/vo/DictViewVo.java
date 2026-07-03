package org.pkaq.sys.dict.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.vo.Vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典视图对象
 *
 * @author PKAQ
 */
@Data
public class DictViewVo implements Vo {
    @Schema(description = "字典 ID")
    private Long id;

    @Schema(description = "字典类型编码")
    private String type;

    @Schema(description = "字典编码")
    private String code;

    @Schema(description = "字典名称")
    private String name;

    @Schema(description = "是否锁定：0-正常，1-已锁定，9999-只读")
    private Integer frozen;

    @Schema(description = "展示顺序")
    private double sort;

    @Schema(description = "字典明细")
    private List<DictLineVo> lines = new ArrayList<>();
}
