package io.nerv.security.provider;

import io.nerv.web.sys.role.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.*;

/**
 * 动态获取url权限配置
 */
@Component
public class UrlFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource {
    @Autowired
    private RoleService roleService;

    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {

        Set<ConfigAttribute> set = new HashSet<>();
        // 获取请求地址
        String requestUrl = ((FilterInvocation) o).getRequestUrl();
		System.out.println("requestUrl >> " + requestUrl);

		// 当前请求需要的权限
        List<Map<String, String>> menusUrl = this.roleService.listRoleNamesWithPath();
        menusUrl.forEach(item -> {
            if(requestUrl.contains(item.get("path"))){
                SecurityConfig securityConfig = new SecurityConfig(item.get("code"));
                set.add(securityConfig);
            }
        });

//      未配置过权限的页面都不需要鉴权，jwtauthfilter已经进行了登录鉴权
//      该过滤器是过滤链中的最后一个，该处判断返回ROLE_USER会使premitall无效
//      如需配置非授权接口均不可访问需修改此处
        if (ObjectUtils.isEmpty(set)) {
            return null;
//            return SecurityConfig.createList("ROLE_USER");
        }
        return set;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return FilterInvocation.class.isAssignableFrom(aClass);
    }
}
