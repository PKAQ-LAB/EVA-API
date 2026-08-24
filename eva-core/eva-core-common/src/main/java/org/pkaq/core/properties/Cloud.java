package org.pkaq.core.properties;

import lombok.Data;
import org.pkaq.core.constant.CommonConstant;

/**
 * 微服务访问配置。
 *
 * @author PKAQ
 */
@Data
public class Cloud {
    /**
     * 是否仅允许经过网关转发的请求访问服务。
     */
    private boolean enable;

    /**
     * 网关转发请求头名称。
     */
    private String requestHeader = CommonConstant.X_GATEWAY_HEADER;

    /**
     * 网关转发请求头期望值。
     */
    private String requestValue = CommonConstant.X_GATEWAY_VALUE;
}
