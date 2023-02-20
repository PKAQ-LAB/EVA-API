package io.nerv.core.auth.security.provider;

import cn.hutool.core.collection.CollUtil;
import io.nerv.core.properties.EvaConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityMetadataSource;
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
    private final SecurityMetadataSource securityMetadataSource;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext requestAuthorizationContext) {
        try {
            // 当前用户的权限信息 比如角色
            Collection<? extends GrantedAuthority> authorities = authentication.get().getAuthorities();
            //
            //authentication.get().isAuthenticated();
            // 当前请求上下文
            // 我们可以获取携带的参数
            Map<String, String> variables = requestAuthorizationContext.getVariables();
            // 我们可以获取原始request对象
            HttpServletRequest request = requestAuthorizationContext.getRequest();
            String requestUrl = new UrlPathHelper().getPathWithinApplication(request);

            log.info(" ：：权限决策 ：：");
            log.info(" 请求地址: [{}] , 当前权限： [{}] , 携带参数: [{}] ", requestUrl, authorities, variables);

            // 预检请求  或未开启资源权限 直接放行
            if (request.getMethod().equals(HttpMethod.OPTIONS)) {
                return new AuthorizationDecision(true);
            }

            Collection<ConfigAttribute> attributes = this.securityMetadataSource.getAttributes(requestAuthorizationContext);

            if (CollUtil.isNotEmpty(attributes)) {
                return new AuthorizationDecision(true);
            } else {
                return new AuthorizationDecision(false);
            }
        } catch (AccessDeniedException ex) {
            return new AuthorizationDecision(false);
        }
    }
}