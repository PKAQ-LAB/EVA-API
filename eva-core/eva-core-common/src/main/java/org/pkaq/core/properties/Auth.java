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

    // JWT配置
    private Jwt jwt;

    // OpenAPI配置
    private OpenApi openApi;

    public boolean isJwtEnabled() {
        return getJwt().isEnabled();
    }

    public boolean isOpenApiEnabled() {
        return getOpenApi().isEnabled();
    }

    public long getSignatureTimestampToleranceSeconds() {
        return getOpenApi().getSignatureTimestampToleranceSeconds();
    }

    public String getSignatureAlgorithm() {
        return getOpenApi().getSignatureAlgorithm();
    }

    public boolean matchJwtPath(String path) {
        return getJwt().matchPath(path);
    }

    public boolean matchOpenApiPath(String path) {
        return getOpenApi().matchPath(path);
    }

    public Jwt getJwt() {
        return null == jwt ? new Jwt() : jwt;
    }

    public OpenApi getOpenApi() {
        return null == openApi ? new OpenApi() : openApi;
    }

    private static boolean matchPath(String path, String[] patterns) {
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
    public static class Jwt {
        // 是否启用JWT认证
        private Boolean enabled;

        // 基于JWT的认证路径(默认全量)
        private String[] paths;

        public boolean isEnabled() {
            return null == enabled || enabled;
        }

        public boolean matchPath(String path) {
            if (!isEnabled()) {
                return false;
            }

            return Auth.matchPath(path, paths);
        }
    }

    @Data
    public static class OpenApi {
        // 是否启用OpenAPI认证
        private Boolean enabled;

        // OpenAPI认证路径(默认全量)
        private String[] paths;

        // OpenAPI Header配置
        private Headers headers;

        // OpenAPI签名配置
        private Signature signature;

        public boolean isEnabled() {
            return null == enabled || enabled;
        }

        public Headers getHeaders() {
            return null == headers ? new Headers() : headers;
        }

        public Signature getSignature() {
            return null == signature ? new Signature() : signature;
        }

        public long getSignatureTimestampToleranceSeconds() {
            if (getSignature().getTimestampToleranceSeconds() == null) {
                return 300L;
            }

            return getSignature().getTimestampToleranceSeconds();
        }

        public String getSignatureAlgorithm() {
            String algorithm = getSignature().getAlgorithm();
            if (algorithm == null || algorithm.isBlank()) {
                return "HmacSHA256";
            }

            return algorithm;
        }

        public boolean matchPath(String path) {
            if (!isEnabled()) {
                return false;
            }

            return Auth.matchPath(path, paths);
        }
    }

    @Data
    public static class Headers {
        // AppKey Header
        private String appKey = "X-App-Key";

        // Timestamp Header
        private String timestamp = "X-Timestamp";

        // Signature Header
        private String signature = "X-Signature";
    }

    @Data
    public static class Signature {
        // 时间戳容忍度(秒)
        private Long timestampToleranceSeconds;

        // 签名算法
        private String algorithm;
    }
}