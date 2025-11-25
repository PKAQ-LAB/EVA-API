package org.pkaq.core.auth.security.provider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.properties.EvaConfig;
import org.pkaq.core.threaduser.ThreadUserHelper;
import org.pkaq.sys.role.service.RoleService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 动态 URL 权限管理器（Spring Security 7.x）
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "eva.resource-permission", name = "enable", havingValue = "true")
public class DynamicSecurityMetadataSource implements AuthorizationManager<RequestAuthorizationContext> {

    private final RoleService roleService;
    private final EvaConfig evaConfig;

    /** 角色 -> 可访问 URL 集合（严格模式） */
    private final Map<String, Set<String>> rolePermMap = new ConcurrentHashMap<>();

    /** 可访问 URL 集合（简单模式） */
    private final Set<String> pathPermSet = ConcurrentHashMap.newKeySet();

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    /** 初始化资源权限 */
    public void loadResources() {
        if (!evaConfig.getResourcePermission().isEnable()) {
            return;
        }

        List<Map<String, String>> menusUrl = this.roleService.listRoleNamesWithPath();

        if (evaConfig.getResourcePermission().isStrict()) {
            menusUrl.forEach(item -> {
                String roleCode = item.get("code");
                String path = buildFullPath(item);
                rolePermMap.computeIfAbsent(roleCode, k -> new HashSet<>()).add(path);
            });
        } else {
            menusUrl.forEach(item -> {
                String path = buildFullPath(item);
                pathPermSet.add(path);
            });
        }
    }

    private String buildFullPath(Map<String, String> item) {
        String path = item.get("path");
        String resourcePath = item.get("resource_url");

        if (resourcePath != null && !resourcePath.isBlank()) {
            resourcePath = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        }

        path = path.endsWith("/") ? path + resourcePath : path + "/" + resourcePath;
        return path;
    }

    @Override
    public AuthorizationDecision authorize(Supplier<? extends Authentication> authenticationSupplier,
                                           RequestAuthorizationContext requestContext) {

        try {
            Authentication authentication = authenticationSupplier.get();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            HttpServletRequest request = requestContext.getRequest();
            String requestUrl = urlPathHelper.getPathWithinApplication(request);

            log.info("权限决策：请求地址 [{}]，用户权限 [{}]", requestUrl, authorities);

            // 预检请求直接放行
            if (HttpMethod.OPTIONS.matches(request.getMethod())) {
                return new AuthorizationDecision(true);
            }

            // 严格模式：根据用户角色匹配 URL
            if (evaConfig.getResourcePermission().isStrict()) {
                String[] userRoles = ThreadUserHelper.getUserRoles();
                if (userRoles == null || userRoles.length == 0) {
                    return new AuthorizationDecision(false);
                }

                boolean granted = Arrays.stream(userRoles)
                        .anyMatch(role -> {
                            Set<String> urls = rolePermMap.get(role);
                            return urls != null && urls.contains(requestUrl);
                        });

                return new AuthorizationDecision(granted);

            } else { // 简单模式：判断 URL 是否在 pathPermSet
                if (pathPermSet.isEmpty()) {
                    return new AuthorizationDecision(false);
                }
                boolean granted = pathPermSet.contains(requestUrl);
                return new AuthorizationDecision(granted);
            }

        } catch (Exception ex) {
            log.error("权限决策异常", ex);
            return new AuthorizationDecision(false);
        }
    }

    /** 启动时加载资源 */
    public void afterPropertiesSet() {
        loadResources();
    }
}
