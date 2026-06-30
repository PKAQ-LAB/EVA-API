package org.pkaq.sys.dev.generator.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.pkaq.core.mvc.bo.Bo;

import java.util.List;

/**
 * AI代码生成提示词参数。
 *
 * @author PKAQ
 */
@Data
@Schema(title = "AI代码生成提示词参数")
public class AiCodePromptBo implements Bo {

    @NotBlank(message = "模块名称不能为空")
    @Schema(description = "模块中文名称，例如客户管理", requiredMode = Schema.RequiredMode.REQUIRED)
    private String moduleName;

    @NotBlank(message = "业务说明不能为空")
    @Schema(description = "业务目标或功能说明", requiredMode = Schema.RequiredMode.REQUIRED)
    private String requirement;

    @Schema(description = "Java包名，例如 org.pkaq.crm.customer")
    private String packageName;

    @Schema(description = "实体类名，例如 Customer")
    private String entityName;

    @Schema(description = "数据库表名，例如 CRM_CUSTOMER")
    private String tableName;

    @Schema(description = "前端路由，例如 /crm/customer")
    private String routeUrl;

    @Schema(description = "功能类型，例如 crud、tree、master-detail")
    private String featureType;

    @Schema(description = "字段列表")
    @Valid
    private List<AiCodeFieldBo> fields;

    @Schema(description = "补充约束，例如权限、校验、排序、冻结等")
    private String extraConstraints;
}
