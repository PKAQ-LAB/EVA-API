package org.pkaq.sys.dict.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.vo.Vo;

import java.util.ArrayList;
import java.util.List;

/**
 * 字典节点视图对象
 *
 * @author PKAQ
 */
@Data
public class DictViewVo implements Vo {
    @Schema(description = "字典节点 ID")
    private Long id;

    @Schema(description = "字典类型编码")
    private String type;

    @Schema(description = "节点编码")
    private String code;

    @Schema(description = "鑺傜偣鍚嶇О")
    private String name;

    @Schema(description = "叶子节点提交值")
    private String value;

    @Schema(description = "上级节点ID")
    private Long pid;

    @Schema(description = "树路径")
    private String path;

    @Schema(description = "是否叶子鑺傜偣")
    private Boolean isleaf;

    @Schema(description = "是否可选")
    private Boolean selectable;

    @Schema(description = "是否冻结：0-未冻结，1-冻结，-1-只读")
    private Integer frozen;

    @Schema(description = "展示顺序")
    private double sort;

    @Schema(description = "子节点列表")
    private List<DictViewVo> children = new ArrayList<>();
}
