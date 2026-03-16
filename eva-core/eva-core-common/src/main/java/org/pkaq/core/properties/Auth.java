package org.pkaq.core.properties;

import lombok.Data;
import org.springframework.util.AntPathMatcher;

/**
 * 鉴权配置
 */
@Data
public class Auth {
    private String[] webstatic;

    // 无需鉴权路径(无条件访问)
    private String[] anonymous;

    // 无需资源鉴权的路径
    private String[] permit;

    // 是否启用jwt认证
    private Boolean jwtEnabled;

    // 是否启用OpenAPI认证
    private Boolean openApiEnabled;

    // 基于jwt的认证路径(默认全量)
    private String[] jwtAuthPaths;

    // 基于appKey / security的认证路径
    private String[] openAPIPaths;

    // OpenAPI签名配置
    private Signature signature;

    public boolean isJwtEnabled() {
        return null == jwtEnabled || jwtEnabled;
    }

    public boolean isOpenApiEnabled() {
        return null == openApiEnabled || openApiEnabled;
    }

    public long getSignatureTimestampToleranceSeconds() {
        if (signature == null || signature.getTimestampToleranceSeconds() == null) {
            return 300L;
        }

        return signature.getTimestampToleranceSeconds();
    }

    public String getSignatureAlgorithm() {
        if (signature == null || signature.getAlgorithm() == null || signature.getAlgorithm().isBlank()) {
            return "HmacSHA256";
        }

        return signature.getAlgorithm();
    }

    public boolean matchJwtPath(String path) {
        if (!isJwtEnabled()) {
            return false;
        }

        return matchPath(path, jwtAuthPaths);
    }

    public boolean matchOpenApiPath(String path) {
        if (!isOpenApiEnabled()) {
            return false;
        }

        return matchPath(path, openAPIPaths);
    }

    private boolean matchPath(String path, String[] patterns) {
        if (path == null) {
            return false;
        }

        if (patterns == null || patterns.length == 0) {
            return true;
        }

        AntPathMatcher matcher = new AntPathMatcher();
        for (String pattern : patterns) {
            if (matcher.match(pattern, path)) {
                return true;
            }
        }

        return false;
    }

    @Data
    public static class Signature {
        // 时间戳容忍度(秒)
        private Long timestampToleranceSeconds;

        // 签名算法
        private String algorithm;
    }
}
