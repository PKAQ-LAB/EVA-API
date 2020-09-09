package io.nerv.filter;

import cn.hutool.core.util.StrUtil;
import com.nimbusds.jose.JWSObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 将登录用户的JWT转化成用户信息的全局过滤器
 */
@Component
@Slf4j
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final String TK = "X-Authorization";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 获取令牌信息
        // 从header中尝试获取
        String token = request.getHeaders().getFirst(TK);

        // 从cookie中尝试获取
        if (StrUtil.isBlank(token)){
            HttpCookie cookie = request.getCookies().getFirst(TK);
            if (null != cookie) {
                token = cookie.getValue();
            }
        }

        // 无法获取token
        if (StrUtil.isEmpty(token)) {
            return chain.filter(exchange);
//            return response.setComplete();
        }

        try {
            //从token中解析用户信息并设置到Header中去
            String realToken = token.replace("Bearer ", "");

            JWSObject jwsObject = JWSObject.parse(realToken);
            String userStr = jwsObject.getPayload().toString();
            log.info("AuthGlobalFilter.filter() user:{}",userStr);

            request = request.mutate().header("user", userStr).build();
            exchange = exchange.mutate().request(request).build();

        } catch (ParseException | java.text.ParseException e) {
            e.printStackTrace();
        }
        return chain.filter(exchange);
    }
 
    @Override
    public int getOrder() {
        return 0;
    }
}
