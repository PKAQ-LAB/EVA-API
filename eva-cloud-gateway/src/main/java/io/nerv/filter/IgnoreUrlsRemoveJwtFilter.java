package io.nerv.filter;

import io.nerv.core.constant.CommonConstant;
import io.nerv.properties.EvaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 白名单路径访问时需要移除JWT请求头
 */
@Component
public class IgnoreUrlsRemoveJwtFilter implements WebFilter {
    @Autowired
    private EvaConfig evaConfig;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        URI uri = request.getURI();
        PathMatcher pathMatcher = new AntPathMatcher();

        var tokenHeader = evaConfig.getJwt().getHeader();
        //白名单路径移除JWT请求头
        String[] ignoreUrls = evaConfig.getSecurity().getAnonymous();
        for (String ignoreUrl : ignoreUrls) {
            if (pathMatcher.match(ignoreUrl, uri.getPath())) {
//                从参数 header cookie里移除token
                request = request.mutate().header(tokenHeader, "").build();
                if ( null != request.getCookies() && request.getCookies().size() > 0 ){
                    request.getCookies().remove(CommonConstant.ACCESS_TOKEN_KEY);
                }

                if (null != request.getQueryParams() && request.getQueryParams().size() > 0){
                    request.getQueryParams().remove(tokenHeader);
                }

                exchange = exchange.mutate().request(request).build();

                return chain.filter(exchange);
            }
        }

        return chain.filter(exchange);
    }
}