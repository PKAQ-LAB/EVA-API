package org.pkaq.core.properties;

import org.pkaq.core.constant.CommonConstant;

import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * EVA 配置语义校验器。
 *
 * @author PKAQ
 */
public final class EvaConfigurationValidator {

    private static final int MINIMUM_JWT_SECRET_BYTES = 32;
    private static final Set<String> SUPPORTED_RUNTIME_MODES = Set.of(
            CommonConstant.MODE_STANDALONE,
            CommonConstant.MODE_SINGLETON,
            CommonConstant.MODE_PLATFORM,
            CommonConstant.MODE_SAAS);
    private static final Set<String> SUPPORTED_TENANT_MODES = Set.of(
            TenantProperties.MODE_STANDALONE,
            TenantProperties.MODE_SCHEMA);

    private EvaConfigurationValidator() {
    }

    /**
     * 校验运行模式、安全能力组合、租户模式和 JWT 密钥。
     *
     * @param config EVA 业务配置
     */
    public static void validate(EvaConfig config) {
        if (null == config) {
            throw new IllegalStateException("EVA 配置不能为空");
        }

        validateMode(config.getMode(), SUPPORTED_RUNTIME_MODES, "eva.mode");
        validateMode(config.getTenant().getMode(), SUPPORTED_TENANT_MODES, "eva.tenant.mode");
        validateSecurityCapabilities(config);

        if (config.getAuth().isJwtEnabled()) {
            validateJwtSecret(config.getJwt().getSecret());
        }
    }

    private static void validateSecurityCapabilities(EvaConfig config) {
        if (config.getAuth().isAuthenticationEnabled()) {
            if (!config.getAuth().isJwtEnabled() && !config.getAuth().isOpenApiEnabled()) {
                throw new IllegalStateException("开启认证时必须启用 JWT 或 OpenAPI 认证机制");
            }
            return;
        }
        if (config.getResourcePermission().isEnable()) {
            throw new IllegalStateException("eva.resource-permission.enable 开启时必须开启认证");
        }
        if (config.getDataPermission().isEnable()) {
            throw new IllegalStateException("eva.data-permission.enable 开启时必须开启认证");
        }
        if (config.getTenant().isEnable()) {
            throw new IllegalStateException("eva.tenant.enable 开启时必须开启认证");
        }
    }

    private static void validateMode(String mode, Set<String> supportedModes, String propertyName) {
        if (null == mode || supportedModes.stream().noneMatch(item -> item.equalsIgnoreCase(mode))) {
            throw new IllegalStateException(propertyName + " 配置值不受支持: " + mode);
        }
    }

    private static void validateJwtSecret(String secret) {
        if (null == secret || secret.isBlank()) {
            throw new IllegalStateException("eva.jwt.secret 必须通过配置文件或环境变量提供");
        }

        if (secret.getBytes(StandardCharsets.UTF_8).length < MINIMUM_JWT_SECRET_BYTES) {
            throw new IllegalStateException("eva.jwt.secret 长度不得少于 32 字节");
        }
    }
}
