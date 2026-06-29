package org.pkaq.core.auth.openapi.log.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.ibatis.type.Alias;
import org.pkaq.core.mybatis.mvc.entity.StdEntity;

/**
 * OpenAPI调用日志实体。
 *
 * @author PKAQ
 */
@Data
@Alias("openApiCallLogEntity")
@TableName("API_CALL_LOG")
@EqualsAndHashCode(callSuper = true)
public class OpenApiCallLogEntity extends StdEntity {
    private String appKey;

    private String appName;

    private String requestPath;

    private String requestMethod;

    private Integer statusCode;

    private Boolean success;

    private Long spendTime;

    private String ip;

    private String errorMsg;
}
