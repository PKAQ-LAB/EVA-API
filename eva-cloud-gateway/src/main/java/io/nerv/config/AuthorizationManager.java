package io.nerv.config;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.ReactiveAuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {
    /**
     * 此处保存的是资源对应的权限，可以从数据库中获取
     */
    private static final Map<String, List<String>> resourceRolesMap = Maps.newConcurrentMap();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void initAuthMap() {
        String sql = "  SELECT" +
                "        DISTINCT m.path, r.code, mr.resource_url" +
                "    FROM" +
                "        sys_module_resources mr," +
                "        sys_role_module rm," +
                "        sys_module m," +
                "        sys_role r" +
                "    WHERE" +
                "        rm.MODULE_ID = m.ID" +
                "        AND" +
                "        rm.ROLE_ID = r.ID" +
                "        AND" +
                "        m.id = mr.module_id" +
                "        AND" +
                "        rm.RESOURCE_ID = mr.ID" +
                "        AND" +
                "        m.isleaf = '1'";
        List<Map<String, Object>> menusUrl = jdbcTemplate.queryForList(sql);

        Map<String, List<String>> pathPermMap = new ConcurrentHashMap<>(menusUrl.size());

        menusUrl.stream().forEach(item -> {
            var path = item.get("path")+"";
            var role_code = item.get("code")+"";
            var resoure_path = item.get("resource_url")+"";

            if (StrUtil.isNotBlank(resoure_path)){
                resoure_path = resoure_path.startsWith("/")? resoure_path.substring(1): resoure_path;
            }

            var resource = path.endsWith("/")? path+resoure_path: path+"/"+resoure_path;

            var roles = pathPermMap.get(resource);

            if (null == roles){
                roles = new ArrayList<>();
            }

            roles.add(role_code);

            pathPermMap.put(resource, roles);
        });
    }

    /**
     * 加载资源，初始化资源变量
     * 角色 - url+资源路径
     */

    @Override
    public Mono<AuthorizationDecision> check(Mono<Authentication> mono, AuthorizationContext authorizationContext) {
        //从Redis中获取当前路径可访问角色列表

        ServerWebExchange exchange = authorizationContext.getExchange();
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 带通配符的可以使用这个进行匹配
        PathMatcher pathMatcher = new AntPathMatcher();

        List<String> authorities = null;
        // 判断请求路径是否拥有匹配得资源权限
        for (var key :resourceRolesMap.keySet()) {
            if (pathMatcher.match(key, path)){
                authorities.addAll(resourceRolesMap.get(key));
            }
        }

        log.info("访问路径:[{}],所需要的权限是:[{}]", path, authorities);

        // option 请求，全部放行
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return Mono.just(new AuthorizationDecision(true));
        }

        // 不在权限范围内的url，全部拒绝
        if (null == authorities || authorities.isEmpty()) {
            return Mono.just(new AuthorizationDecision(false));
        }

        return mono
                // 判断是否认证成功
                // 如果token验证通过即Authentication对象的isAuthenticated返回true则向下执行，否则传递为空
                .filter(Authentication::isAuthenticated)
                .filter(a -> a instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .doOnNext(token -> {
                    System.out.println(token.getToken().getHeaders());
                    System.out.println(token.getTokenAttributes());
                })
                //将授予用户的角色权限集合平铺展开，将Authentication中的authorities展开成可迭代对象 Mono对象变成Flux对象即由单元素List转化成多元素List中的元素
                .flatMapIterable(AbstractAuthenticationToken::getAuthorities)
                // 当前用户的授予的角色 role
                .map(GrantedAuthority::getAuthority)
                //只要authorities包含其中一个
                 .any(authorities::contains)
                //授权通过
                .map(AuthorizationDecision::new)
                //默认不允许通过
                .defaultIfEmpty(new AuthorizationDecision(false));

    }

}
