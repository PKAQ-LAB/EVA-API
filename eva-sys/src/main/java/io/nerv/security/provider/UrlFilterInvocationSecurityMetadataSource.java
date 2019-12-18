package io.nerv.security.provider;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import io.nerv.core.util.SecurityHelper;
import io.nerv.web.sys.role.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;

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

    @Value("${eva.datapermission.enable}")
    private boolean datapermissionEnable;
    /**
     * 资源权限 角色，资源路径
     */
    private volatile ConcurrentHashMap<String, Collection<ConfigAttribute>> rolePermMap = null;

    /**
     * 加载资源，初始化资源变量
     */
    public void loadResourceurlPermMap() {
        List<Map<String, String>> menusUrl = this.roleService.listRoleNamesWithPath();
        int hashSize = menusUrl.size();

        rolePermMap = new ConcurrentHashMap<>(hashSize);

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

    @Override
    public Collection<ConfigAttribute> getAttributes(Object o) throws IllegalArgumentException {

        Collection<ConfigAttribute> set = new ArrayList<>();
        // 获取请求地址
        String requestUrl = ((FilterInvocation) o).getRequestUrl();
        log.info("请求URL >> " + requestUrl);
        log.info("当前权限：" + securityHelper.getAuthentication());

//        Arrays.stream(permit).forEach(item -> {
//            ConfigAttribute securityConfig = new SecurityConfig(item);
//            set.add(securityConfig);
//        });

        if (datapermissionEnable && null != securityHelper.getAuthentication()){
            Arrays.stream(securityHelper.getRoleNames()).forEach(item -> {
                if (null != rolePermMap.get(item)){
                    set.addAll(rolePermMap.get(item));
                }
            });
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
        loadResourceurlPermMap() ;
    }
}
