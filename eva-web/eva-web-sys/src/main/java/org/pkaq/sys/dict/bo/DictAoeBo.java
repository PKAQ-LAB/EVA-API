package org.pkaq.sys.dict.bo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.pkaq.core.mvc.bo.Bo;
import org.pkaq.sys.dict.entity.DictItemEntity;

import java.util.List;

/**
 * 字典编辑BO
 */
@Data
@Schema(title = "字典编辑BO")
public class DictAoeBo implements Bo {
    private String id;

    @Schema(description = "乐观锁")
    private int revision;

    @Schema(description = "是否冻结（0000 - 未冻结，0001 - 冻结， 9999 - 不可编辑）")
    private String frozen;

    @Schema(description = "展示顺序")
    private int sort;

    @Schema(description = "备注")
    private String remark;

    @NotBlank(message = "{sys.dict.category.empty}")
    @Schema(description = "归属类型")
    private String category;

    @NotBlank(message = "{sys.dict.code.empty}")
    @Schema(description = "字典编码")
    private String code;

    @NotBlank(message = "{sys.dict.name.empty}")
    @Schema(description = "字典名称")
    private String name;

    @Schema(description = "上级节点")
    private String pid;

    @Schema(description = "树路径(id)")
    private String path;

    @Schema(description = "字典项明细")
    private List<DictItemEntity> lines;
}