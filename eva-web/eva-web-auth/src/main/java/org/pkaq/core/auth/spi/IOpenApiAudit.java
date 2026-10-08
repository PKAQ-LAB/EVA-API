package org.pkaq.core.auth.spi;

import jakarta.servlet.http.HttpServletRequest;
import org.pkaq.core.auth.spi.model.AppCredentialSnapshot;

/**
 * OpenAPI调用审计写入契约。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public interface IOpenApiAudit {
    /** 保存调用结果，失败不得改变接口主流程。 */
    void save(HttpServletRequest request, AppCredentialSnapshot credential, String requestPath,
              long startTime, int statusCode, String errorMsg);
}
