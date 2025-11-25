package org.pkaq.core.auth.security.provider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.pkaq.core.util.CollUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author PKAQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "eva.resource-permission", name = "enable", havingValue = "true")
public final class DynamiclAccessDecisionManager implements AuthorizationManager<RequestAuthorizationContext> {
    // 使用你自己的 URL -> 权限映射
    private final Map<String, Collection<String>> urlPermissionMap;

    @Override
    public AuthorizationDecision authorize(
            Supplier<? extends Authentication> authenticationSupplier,
            RequestAuthorizationContext requestContext) {

        try {
            // 当前用户的权限信息 比如角色
            Authentication authentication = authenticationSupplier.get();
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

            //authentication.get().isAuthenticated();
            // 当前请求上下文
            // 我们可以获取携带的参数
            HttpServletRequest request = requestContext.getRequest();
            Map<String, String> variables = requestContext.getVariables();
            String requestUrl = new UrlPathHelper().getPathWithinApplication(request);
            String httpMethod = request.getMethod();

            log.info("：：权限决策 ：：");
            log.info(" 请求地址: [{}] , 当前权限： [{}] , 携带参数: [{}] ", requestUrl, authorities, variables);

            // 预检请求直接放行
            if (HttpMethod.OPTIONS.matches(httpMethod)) {
                return new AuthorizationDecision(true);
            }

            // 根据 URL 获取需要的权限
            Collection<String> requiredPermissions = urlPermissionMap.get(requestUrl);

            // 如果没有配置权限，默认拒绝
            if (CollUtils.isEmpty(requiredPermissions)) {
                return new AuthorizationDecision(false);
            }

            // 判断当前用户是否拥有任意一个权限
            boolean granted = authorities.stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(requiredPermissions::contains);

            return new AuthorizationDecision(granted);

        } catch (Exception ex) {
            log.error("权限决策异常", ex);
            return new AuthorizationDecision(false);
        }
    }
}