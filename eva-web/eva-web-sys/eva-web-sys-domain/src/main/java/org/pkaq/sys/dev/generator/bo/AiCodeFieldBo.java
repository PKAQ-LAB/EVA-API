package org.pkaq.sys.dev.generator.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.bo.Bo;

/**
 * AI代码生成字段参数。
 *
 * @author PKAQ
 */
@Data
@Schema(title = "AI代码生成字段参数")
public class AiCodeFieldBo implements Bo {

    @Schema(description = "字段英文名，例如 account")
    private String name;

    @Schema(description = "字段中文名，例如账号")
    private String title;

    @Schema(description = "Java类型，例如 String、Long、Integer")
    private String javaType;

    @Schema(description = "是否必填")
    private Boolean required;

    @Schema(description = "是否作为列表查询条件")
    private Boolean searchable;

    @Schema(description = "是否在列表展示")
    private Boolean listVisible;
}
