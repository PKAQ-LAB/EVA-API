package org.pkaq.core.auth.config;

import org.pkaq.core.properties.EvaConfig;

/**
 * 自助注册的统一公开入口条件，配置失效时安全关闭。
 *
 * @author PKAQ
 * @date 2026-10-08
 */
public final class RegistrationPolicy {
    private RegistrationPolicy() {
    }

    /** 判断注册是否仅在独立模式和有效JWT认证下开启。 */
    public static boolean isEnabled(EvaConfig config) {
        return config.getAuth().getRegistration().isEnabled() && "standalone".equalsIgnoreCase(config.getMode())
                && !config.getTenant().isEnable() && config.getAuth().isJwtEnabled();
    }

    /** 判断请求是否为已开启的精确POST注册入口。 */
    public static boolean isPublicRequest(EvaConfig config, String method, String path) {
        return isEnabled(config) && "POST".equals(method) && "/auth/register".equals(path);
    }
}
