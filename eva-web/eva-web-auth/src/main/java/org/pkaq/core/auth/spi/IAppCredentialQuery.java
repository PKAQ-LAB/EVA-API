package org.pkaq.core.auth.spi;

import org.pkaq.core.auth.spi.model.AppCredentialSnapshot;

/**
 * OpenAPI应用凭据查询契约。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public interface IAppCredentialQuery {
    /** 获取认证所需凭据快照，不存在时返回空。 */
    AppCredentialSnapshot findCredential(String appKey);
}
