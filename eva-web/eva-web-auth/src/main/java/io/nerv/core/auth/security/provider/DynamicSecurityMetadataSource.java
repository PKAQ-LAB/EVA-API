package io.nerv.core.auth.security.provider;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import io.nerv.core.properties.EvaConfig;
import io.nerv.core.threaduser.ThreadUserHelper;
import io.nerv.sys.role.service.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UrlPathHelper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态获取url权限配置
 *
 * @author
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "eva.resource-permission", name = "enable", havingValue = "true")
public class DynamicSecurityMetadataSource implements FilterInvocationSecurityMetadataSource, InitializingBean {
    private final RoleService roleService;

    private final EvaConfig evaConfig;

    /**
     * 资源权限 角色 - 资源路径 的map
     */
    private volatile Map<String, Collection<ConfigAttribute>> rolePermMap = new ConcurrentHashMap<>();

    /**
     * 资源权限 资源路径 - 角色 的map
     */
    private volatile Set<String> pathPermSet = new ConcurrentHashSet<>();

    private Collection<ConfigAttribute> getValues(Map<String, String> item) {
        var path = item.get("path");
        var role_code = item.get("code");
        var resoure_path = item.get("resource_url");

        if (StrUtil.isNotBlank(resoure_path)) {
            resoure_path = resoure_path.startsWith("/") ? resoure_path.substring(1) : resoure_path;
        }

        path = path.endsWith("/") ? path + resoure_path : path + "/" + resoure_path;

        Collection<ConfigAttribute> values = rolePermMap.get(role_code);

        ConfigAttribute securityConfig = new SecurityConfig(path);

        if (null == values) {
            values = new ArrayList<>();
        }

        values.add(securityConfig);

        return values;
    }

    /**
     * 加载资源，初始化资源变量
     * 角色 - url+资源路径
     */
    public void loadResourceRoleUrlPermMap(List<Map<String, String>> menusUrl) {

        menusUrl.stream().forEach(item -> {
            var role_code = item.get("code");
            rolePermMap.put(role_code, this.getValues(item));
        });
    }

    /**
     * 加载资源，初始化资源变量
     * url+资源路径 - 角色
     */
    public void loadResourceUrlRolePermMap(List<Map<String, String>> menusUrl) {

        menusUrl.stream().forEach(item -> {
            var path = item.get("path");
            var resoure_path = item.get("resource_url");

            if (StrUtil.isNotBlank(resoure_path)) {
                resoure_path = resoure_path.startsWith("/") ? resoure_path.substring(1) : resoure_path;
            }

            path = path.endsWith("/") ? path + resoure_path : path + "/" + resoure_path;

            pathPermSet.add(path);
        });
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {

        if (!evaConfig.getResourcePermission().isEnable()){
            return null;
        }

        // 角色列表
        Collection<ConfigAttribute> set = new ArrayList<>();
        // 获取请求地址

        HttpServletRequest request = ((RequestAuthorizationContext) o).getRequest();
        String requestUrl = new UrlPathHelper().getPathWithinApplication(request);

        var roles = ThreadUserHelper.getUserRoles();

        if (ArrayUtil.isNotEmpty(roles)) {

            /**
             *    严格鉴权模式 仅允许访问授权资源 未授权资源一律禁止访问
             *    根据用户角色获取所有可访问资源路径 置入 Collection<ConfigAttribute>
             */
            if (evaConfig.getResourcePermission().isStrict()) {
                Arrays.stream(roles).forEach(item -> {
                    if (null != rolePermMap.get(item)) {
                        set.addAll(rolePermMap.get(item));
                    }
                });
            } else {
                /**
                 *  简单鉴权模式 可访问所有无权限要求的资源
                 *  根据资源路径获取访问该资源需要的所有角色 置入 Collection<ConfigAttribute>
                 */
                pathPermSet.forEach((v) -> {
                    var urlMatcher = new AntPathRequestMatcher(v);
                    if (urlMatcher.matches(request) || StrUtil.equals(requestUrl, v)) {
                        ConfigAttribute securityConfig = new SecurityConfig(v);
                        set.add(securityConfig);
                    }
                });
            }
        }

//      未配置过权限的页面都不需要鉴权，jwtauthfilter已经进行了登录鉴权
//      该过滤器是过滤链中的最后一个，该处判断返回ROLE_USER会使 premitall 无效
//      如需配置非授权接口均不可访问需修改此处
        if (CollectionUtil.isEmpty(set)) {
            return null;
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
        // 加载权限和路径关系
        if (evaConfig.getResourcePermission().isEnable()) {
            List<Map<String, String>> menusUrl = this.roleService.listRoleNamesWithPath();

            if (evaConfig.getResourcePermission().isStrict()) {
                loadResourceRoleUrlPermMap(menusUrl);
            } else {
                loadResourceUrlRolePermMap(menusUrl);
            }
        }
    }
}
