package io.nerv.core.auth.security.provider;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.properties.EvaConfig;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;

/**
 *
 * @author PKAQ
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UrlAccessDecisionManager implements AuthorizationManager<FilterInvocation> {

    @Value("${eva.security.permit}")
    private String[] permit;

    private final EvaConfig evaConfig;

    private final UrlFilterInvocationSecurityMetadataSource urlFilterInvocationSecurityMetadataSource;

    /**
     *   configAttributes 为MyInvocationSecurityMetadataSource的getAttributes(Object object)这个方法返回的结果，
     *   此方法是为了判定用户请求的url 是否在权限表中，如果在权限表中，则返回给 decide 方法，用来判定用户是否有此权限。如果不在权限表中则放行。
     *
     * @param authentication authentication 是释循环添加到 GrantedAuthority 对象中的权限信息集合.
     * @param o  object 包含客户端发起的请求的requset信息，可转换为 HttpServletRequest request = ((FilterInvocation) object).getHttpRequest();
     * @return
     */

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, FilterInvocation o) {
        HttpServletRequest request = o.getHttpRequest();
        String requestUrl = o.getRequestUrl();

        Collection<ConfigAttribute> configAttributes = urlFilterInvocationSecurityMetadataSource.getAttributes(o);

        log.info("可访问的资源 ：" + configAttributes);
        log.info("requestUrl ： " + requestUrl);

        // 是否开启了资源权限
        boolean resourceEnable = evaConfig.getResourcePermission().isEnable();
        // 是否严格模式
        // 严格模式（白名单）,  仅可访问已授权的资源路径
        // 简单模式（黑名单） , 可访问除配置以外的所有路径
        boolean isStrict = evaConfig.getResourcePermission().isStrict();

        boolean match;
        // 无需鉴权 可访问资源放行
        match = Arrays.stream(permit).anyMatch(item -> {
            var urlMatcher = new AntPathRequestMatcher(item);
            return (urlMatcher.matches(request) ||
                    StrUtil.equals(requestUrl, item));
        });

        if (match || !resourceEnable){
            return new AuthorizationDecision(true);
        }

        if (isStrict) {
            match = configAttributes.stream().anyMatch(item -> {
                String url = item.getAttribute();
                var urlMatcher = new AntPathRequestMatcher(url);
                return (urlMatcher.matches(request) ||
                        StrUtil.equals(requestUrl, url));
            });
        } else {
            // 用户拥有的所有权限
            Collection<? extends GrantedAuthority> authorities = authentication.get().getAuthorities();
            match = configAttributes.stream().anyMatch(item -> {
                String role = item.getAttribute();

                return authorities.stream().anyMatch(ga -> ga.getAuthority().equalsIgnoreCase(role));
            });
            // end for
        }

        return new AuthorizationDecision(match);
    }
}
