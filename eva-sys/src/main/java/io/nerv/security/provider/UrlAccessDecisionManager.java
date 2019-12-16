package io.nerv.security.provider;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.enums.BizCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

@Slf4j
@Component
public class UrlAccessDecisionManager implements AccessDecisionManager {

    @Value("${eva.security.permit}")
    private String[] permit;

    // configAttributes 为MyInvocationSecurityMetadataSource的getAttributes(Object object)这个方法返回的结果，
    // authentication 是释循环添加到 GrantedAuthority 对象中的权限信息集合.
    // object 包含客户端发起的请求的requset信息，可转换为 HttpServletRequest request = ((FilterInvocation) object).getHttpRequest();
    // configAttributes 为 UrlFilterInvocationSecurityMetadataSource 的getAttributes(Object object)这个方法返回的结果，
    // 此方法是为了判定用户请求的url 是否在权限表中，如果在权限表中，则返回给 decide 方法，用来判定用户是否有此权限。如果不在权限表中则放行。
    @Override
    public void decide(Authentication authentication, Object o,
                       Collection<ConfigAttribute> collection) throws AccessDeniedException {
        log.info("collection=" + collection);

        HttpServletRequest request = ((FilterInvocation) o).getHttpRequest();
        String requestUrl = ((FilterInvocation) o).getRequestUrl();
        log.info("requestUrl ： " + requestUrl);

        // 可访问资源放行
        for (var permitUrl : permit){
            var urlMatcher = new AntPathRequestMatcher(permitUrl);

            if (urlMatcher.matches(request) ||
                    StrUtil.equals(requestUrl,permitUrl)) return;
        }

        for (ConfigAttribute configAttribute : collection) {
            // 当前请求需要的权限
            String url = configAttribute.getAttribute();

            var urlMatcher = new AntPathRequestMatcher(url);

            if (urlMatcher.matches(request) ||
                StrUtil.equals(requestUrl,url)) return;
        }
        throw new AccessDeniedException(BizCodeEnum.PERMISSION_DENY.getName());
    }

    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
