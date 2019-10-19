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
    // 此方法是为了判定用户请求的url 是否在权限表中，如果在权限表中，则返回给 decide 方法，
    // 用来判定用户是否有此权限。
    @Override
    public void decide(Authentication authentication, Object o,
                       Collection<ConfigAttribute> collection) throws AccessDeniedException {
        log.debug("collection=" + collection);

        HttpServletRequest request = ((FilterInvocation) o).getHttpRequest();
        String requestUrl = ((FilterInvocation) o).getRequestUrl();

        for (ConfigAttribute configAttribute : collection) {
            // 当前请求需要的权限
            String url = configAttribute.getAttribute();
            log.debug("当前请求资源为 ： " + url);
            var urlMatcher = new AntPathRequestMatcher(url);

            if (urlMatcher.matches(request)|| StrUtil.equals(requestUrl,url) || StrUtil.equalsAny(requestUrl, this.permit)) return;
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
