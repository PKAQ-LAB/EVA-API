package io.nerv.security.provider;

import cn.hutool.core.util.StrUtil;
import io.nerv.core.util.SecurityHelper;
import io.nerv.web.sys.role.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态获取url权限配置
 */
@Slf4j
@Component
public class UrlFilterInvocationSecurityMetadataSource implements FilterInvocationSecurityMetadataSource, InitializingBean {
    @Autowired
    private RoleService roleService;

    @Autowired
    private SecurityHelper securityHelper;
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
            var resoure_path = item.get("resource_url");

            if (StrUtil.isNotBlank(resoure_path)){
                resoure_path = resoure_path.startsWith("/")? resoure_path.substring(1): resoure_path;
            }

            path = path.endsWith("/")? path+resoure_path: path+"/"+resoure_path;

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

        HttpServletRequest request = ((FilterInvocation) o).getHttpRequest();

        Collection<ConfigAttribute> set = null;
        // 获取请求地址
        String requestUrl = ((FilterInvocation) o).getRequestUrl();
        log.debug("requestUrl >> " + requestUrl);

		// 当前请求需要的权限
        var keys = urlPermMap.keys().asIterator();
        while (keys.hasNext()){
            var key = keys.next();
            var urlMatcher = new AntPathRequestMatcher(key);

            if (urlMatcher.matches(request)|| StrUtil.equals(requestUrl,key)) {
                if (null == set){
                    set = new HashSet<>();
                }
                set.addAll(urlPermMap.get(key));
            }
        }

//      未配置过权限的页面都不需要鉴权，jwtauthfilter已经进行了登录鉴权
//      该过滤器是过滤链中的最后一个，该处判断返回ROLE_USER会使 premitall 无效
//      如需配置非授权接口均不可访问需修改此处
        if (securityHelper.isAdmin()){
            return null;
        }

        if (ObjectUtils.isEmpty(set)) {
            return SecurityConfig.createList("ROLE_USER");
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
