package io.nerv.security.provider;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.nerv.core.util.SecurityHelper;
import io.nerv.properties.EvaConfig;
import io.nerv.web.sys.role.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.stereotype.Component;

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

    @Autowired
    private EvaConfig evaConfig;

    /**
     * 资源权限 角色 - 资源路径 的map
     */
    private volatile ConcurrentHashMap<String, Collection<ConfigAttribute>> rolePermMap = null;

    /**
     * 资源权限 资源路径 - 角色 的map
     */
    private volatile ConcurrentHashMap<String, Collection<ConfigAttribute>> pathPermMap = null;

    /**
     * 加载资源，初始化资源变量
     * 角色 - url+资源路径
     */
    public void loadResourceRoleUrlPermMap(List<Map<String, String>> menusUrl) {

        rolePermMap = new ConcurrentHashMap<>(menusUrl.size());

        menusUrl.stream().forEach(item -> {
            var path = item.get("path");
            var role_code = item.get("code");
            var resoure_path = item.get("resource_url");

            if (StrUtil.isNotBlank(resoure_path)){
                resoure_path = resoure_path.startsWith("/")? resoure_path.substring(1): resoure_path;
            }

            path = path.endsWith("/")? path+resoure_path: path+"/"+resoure_path;

            Collection<ConfigAttribute> values = rolePermMap.get(role_code);

            ConfigAttribute securityConfig = new SecurityConfig(path);

            if (null == values){
                values = new ArrayList<>();
            }

            values.add(securityConfig);

            rolePermMap.put(role_code, values);
        });
    }

    /**
     * 加载资源，初始化资源变量
     * url+资源路径 - 角色
     */
    public void loadResourceUrlRolePermMap(List<Map<String, String>> menusUrl) {

        pathPermMap = new ConcurrentHashMap<>(menusUrl.size());

        menusUrl.stream().forEach(item -> {
            var path = item.get("path");
            var role_code = item.get("code");
            var resoure_path = item.get("resource_url");

            if (StrUtil.isNotBlank(resoure_path)){
                resoure_path = resoure_path.startsWith("/")? resoure_path.substring(1): resoure_path;
            }

            var key = path.endsWith("/")? path+resoure_path: path+"/"+resoure_path;

            Collection<ConfigAttribute> values = pathPermMap.get(key);

            ConfigAttribute securityConfig = new SecurityConfig(role_code);

            if (null == values){
                values = new ArrayList<>();
            }

            values.add(securityConfig);

            pathPermMap.put(key, values);
        });
    }

    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {

        // 角色列表
        Collection<ConfigAttribute> set = new ArrayList<>();
        // 获取请求地址
        String requestUrl = ((FilterInvocation) o).getRequestUrl();
        log.info("请求URL >> " + requestUrl);
        log.info("当前权限：" + securityHelper.getAuthentication());

        // 未开启资源权限 直接返回
        if (!evaConfig.getResourcePermission().isEnable()){
            return null;
        }

        if (null != securityHelper.getAuthentication()){
            /**
             *    严格鉴权模式 仅允许访问授权资源 未授权资源一律禁止访问
             *    根据用户角色获取所有可访问资源路径 置入 Collection<ConfigAttribute>
             */
            if (evaConfig.getResourcePermission().isStrict()){
                Arrays.stream(securityHelper.getRoleNames()).forEach(item -> {
                    if (null != rolePermMap.get(item)){
                        set.addAll(rolePermMap.get(item));
                    }
                });
            } else {
                /**
                 *  简单鉴权模式 可访问所有无权限要求的资源
                 *  根据资源路径获取访问该资源需要的所有角色 置入 Collection<ConfigAttribute>
                 */

                HttpServletRequest request = ((FilterInvocation) o).getHttpRequest();

                pathPermMap.forEach((k, v) -> {
                    var urlMatcher = new AntPathRequestMatcher(k);
                    if (urlMatcher.matches(request) || StrUtil.equals(requestUrl,k)){
                        set.addAll(v);
                    }
                });
            }
        }

//      未配置过权限的页面都不需要鉴权，jwtauthfilter已经进行了登录鉴权
//      该过滤器是过滤链中的最后一个，该处判断返回ROLE_USER会使 premitall 无效
//      如需配置非授权接口均不可访问需修改此处
        if (CollectionUtil.isEmpty(set)){
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
        if (evaConfig.getResourcePermission().isEnable()){
            List<Map<String, String>> menusUrl = this.roleService.listRoleNamesWithPath();

            if (evaConfig.getResourcePermission().isStrict()){
                loadResourceRoleUrlPermMap(menusUrl);
            } else {
                loadResourceUrlRolePermMap(menusUrl);
            }
        }
    }
}
