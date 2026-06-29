package org.pkaq.core.auth.openapi.log.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.pkaq.core.mvc.bo.PageBo;

import java.time.LocalDateTime;

/**
 * OpenAPI调用日志查询参数。
 *
 * @author PKAQ
 */
@Schema(description = "OpenAPI调用日志查询参数")
@Data
@EqualsAndHashCode(callSuper = false)
public class OpenApiCallLogQueryBo extends PageBo {

    @Schema(description = "AppKey")
    private String appKey;

    @Schema(description = "应用名称")
    private String appName;

    @Schema(description = "请求路径")
    private String requestPath;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "开始时间")
    private LocalDateTime begin;

    @Schema(description = "结束时间")
    private LocalDateTime end;
}
