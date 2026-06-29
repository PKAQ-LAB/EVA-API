package org.pkaq.core.auth.openapi.log.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.pkaq.core.mvc.vo.Vo;

import java.time.LocalDateTime;

/**
 * OpenAPI调用日志视图对象。
 *
 * @author PKAQ
 */
@Schema(description = "OpenAPI调用日志视图对象")
@Data
public class OpenApiCallLogVo implements Vo {
    private Long id;

    private String appKey;

    private String appName;

    private String requestPath;

    private String requestMethod;

    private Integer statusCode;

    private Boolean success;

    private Long spendTime;

    private String ip;

    private String errorMsg;

    private LocalDateTime utcCreate;
}
