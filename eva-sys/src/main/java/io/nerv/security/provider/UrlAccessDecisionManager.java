package io.nerv.security.provider;

import io.nerv.core.enums.ErrorCodeEnum;
import io.nerv.security.exception.OathException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Slf4j
@Component
public class UrlAccessDecisionManager implements AccessDecisionManager {
    // 鉴权模式
    @Value("${eva.security.mode}")
    private String mode;

    // configAttributes 为MyInvocationSecurityMetadataSource的getAttributes(Object object)这个方法返回的结果，
    // 此方法是为了判定用户请求的url 是否在权限表中，如果在权限表中，则返回给 decide 方法，
    // 用来判定用户是否有此权限。
    @Override
    public void decide(Authentication authentication, Object o,
                       Collection<ConfigAttribute> collection) throws AccessDeniedException {
        log.debug("collection=" + collection);
        for (ConfigAttribute configAttribute : collection) {
            // 当前请求需要的权限
            String needRole = configAttribute.getAttribute();
            log.debug("needRole=" + needRole);
            if ("ROLE_USER".equals(needRole)) {
                // 所有未授权的菜单都会被认定为 需要 ROLE_USER 权限，
                // 采用严格模式，只允许授权的url进行访问 所有未授权的不许访问
                // 也可采用简单模式， 只对授权的url进行鉴权，未进行过授权配置的url都可访问
                // 若采用第二种方式，可以通过用户登录后默认授予ROLE_USER权限 或 放开下面的注释即可
                if ("strict".equals(this.mode)) {
                    throw new OathException(ErrorCodeEnum.PERMISSION_DENY);
                } else {
                    return;
                }
            }
            // 当前用户所具有的权限
            Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
            log.debug("authorities=" + authorities);
            for (GrantedAuthority grantedAuthority : authorities) {
                if (grantedAuthority.getAuthority().equals(needRole)) {
                    return;
                }
            }
        }
        throw new AccessDeniedException(ErrorCodeEnum.PERMISSION_DENY.getName());
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
