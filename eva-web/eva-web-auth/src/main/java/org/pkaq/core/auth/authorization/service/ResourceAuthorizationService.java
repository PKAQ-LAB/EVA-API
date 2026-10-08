package org.pkaq.core.auth.authorization.service;

import lombok.RequiredArgsConstructor;
import org.pkaq.core.auth.spi.IResourcePermissionQuery;
import org.pkaq.core.properties.EvaConfig;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * 请求资源权限判定，与身份认证和数据范围组装分离。
 *
 * @author PKAQ
 * @date 2026-10-07
 */
@Service
@RequiredArgsConstructor
public class ResourceAuthorizationService {
    private final EvaConfig evaConfig;
    private final IResourcePermissionQuery roleResourceCacheService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 判断已认证用户是否允许访问资源。
     *
     * @param tenantId 租户编号
     * @param roleIds 可信角色编号
     * @param method 请求方法
     * @param path 请求路径
     * @return 是否允许
     */
    public boolean hasPermission(long tenantId, List<Long> roleIds, String method, String path) {
        if (!evaConfig.getResourcePermission().isEnable() || isPermitPath(path)) {
            return true;
        }
        return null != roleIds && !roleIds.isEmpty()
                && roleResourceCacheService.hasPermission(tenantId, roleIds, method, path);
    }

    private boolean isPermitPath(String path) {
        String[] paths = evaConfig.getAuth().getPermit();
        if (null == paths) {
            return false;
        }
        for (String pattern : paths) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }
}
