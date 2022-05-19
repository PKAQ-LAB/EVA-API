package io.nerv.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
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

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AuthorizationManager implements ReactiveAuthorizationManager<AuthorizationContext> {
    /**
     * 此处保存的是资源对应的权限，可以从数据库中获取
     */
    private static final Map<String, List<String>> resourceRolesMap = Maps.newConcurrentMap();

    @PostConstruct
    public void initAuthMap() {
        //TODO 系统启动的时候执行一次
        // 这里需要通过数据库获取角色权限信息
        resourceRolesMap.put("/admin/hello", CollUtil.toList("USER"));
        resourceRolesMap.put("/admin/user/currentUser", CollUtil.toList("ADMIN", "TEST"));

    }
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
                authorities = resourceRolesMap.get(key);
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

        mono.filter(a -> a.isAuthenticated())
                .flatMapIterable( a -> a.getAuthorities())
                .map( g-> g.getAuthority())
                .any(c->{
                    //检测权限是否匹配
                    String[] roles = c.split(",");
                    for(String role:roles) {
                        System.out.println(role);
                    }
                    return false;
                })
                .map( hasAuthority -> new AuthorizationDecision(hasAuthority))
                .defaultIfEmpty(new AuthorizationDecision(false));

        // 判断JWT中携带的用户角色是否有权限访问
        var x = mono
                .filter(Authentication::isAuthenticated)
                .flatMapIterable(Authentication::getAuthorities)
                .map(GrantedAuthority::getAuthority)
                .any(roleId -> {
                    // 5. roleId是请求用户的角色(格式:ROLE_{roleId})，authorities是请求资源所需要角色的集合
                    log.info("访问路径：{}", path);
                    log.info("用户角色roleId：{}", roleId);
                    log.info("资源需要权限authorities：{}","111");
                    return true;
                })
                .map(AuthorizationDecision::new)
                .defaultIfEmpty(new AuthorizationDecision(false));


        var s=  mono
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

        return x;
    }

}
