package org.pkaq.sys.dict.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.pkaq.core.mvc.bo.Bo;

/**
 * 字典节点新增编辑参数
 *
 * @author PKAQ
 */
@Data
@Schema(title = "字典节点编辑BO")
public class DictAoeBo implements Bo {
    @Schema(description = "字典节点 ID")
    private Long id;

    @Schema(description = "乐观锁")
    private Integer revision;

    @Schema(description = "是否冻结：0-未冻结，1-冻结，-1-只读")
    private Integer frozen;

    @Schema(description = "展示顺序")
    private double sort;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "字典类型编码，根节点默认等于 code，子节点默认继承父节点 type")
    private String type;

    @NotBlank(message = "{sys.dict.code.empty}")
    @Schema(description = "节点编码")
    private String code;

    @NotBlank(message = "{sys.dict.name.empty}")
    @Schema(description = "鑺傜偣鍚嶇О")
    private String name;

    @Schema(description = "叶子节点提交值，为空时默认等于code")
    private String value;

    @Schema(description = "上级节点 ID，根节点为 0")
    private Long pid;

    @Schema(description = "树路径")
    private String path;
}
