package io.nerv.security.provider;

import io.nerv.web.sys.role.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态获取url权限配置
 */
@Slf4j
@Component
public class UrlFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource, InitializingBean {
    @Autowired
    private RoleService roleService;

    /**
     * 资源权限
     */
    private volatile ConcurrentHashMap<String, Collection<ConfigAttribute>> urlPermMap = null;

    /**
     * 加载资源，初始化资源变量
     */
    public void loadResourceurlPermMap() {
        List<Map<String, String>> menusUrl = this.roleService.listRoleNamesWithPath();
        int hashSize = menusUrl.size();

        urlPermMap = new ConcurrentHashMap<>(hashSize);

        menusUrl.stream().forEach(item -> {
            var path = item.get("path");
            var role_code = item.get("code");

            Collection<ConfigAttribute> values = urlPermMap.get(path);

            ConfigAttribute securityConfig = new SecurityConfig(role_code);

            if (null == values){
                values = new ArrayList<>();
            }

            values.add(securityConfig);
            urlPermMap.put(path, values);
        });
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {

        Collection<ConfigAttribute> set = null;
        // 获取请求地址
        String requestUrl = ((FilterInvocation) o).getRequestUrl();
        log.debug("requestUrl >> " + requestUrl);

		// 当前请求需要的权限
        var keys = urlPermMap.keys().asIterator();
        while (keys.hasNext()){
            var key = keys.next();
            if(requestUrl.contains(key)){
                return urlPermMap.get(key);
            }
            // TODO 判断 全部资源: /sys/role/** 部分资源 /sys/role/list
//            urlMatcher = new AntPathRequestMatcher(resURL);
//
//            if (urlMatcher.matches(request)||StringUtils.equals(request.getRequestURI(),resURL)) {
//                attrSet.addAll(attrMap.get(resURL));
//            }

        }

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

    /**
     * 启动时加载资源
     */
    @Override
    public void afterPropertiesSet() {
        loadResourceurlPermMap() ;
    }
}
