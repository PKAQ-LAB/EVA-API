package io.nerv.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.nimbusds.jose.JWSObject;
import io.nerv.config.AuthConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.expression.ParseException;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 将登录用户的JWT转化成用户信息的全局过滤器
 */
@Component
@Slf4j
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final String TK = "X-Authorization";

    @Autowired
    private AuthConfig authConfig;

    // url匹配器
    private AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 无需认证的路径
        boolean anonymous = false;

        String requestUrl = request.getPath().value();

        for (String igUrl : authConfig.getAnonymous()){
            if (pathMatcher.match(igUrl, requestUrl)) {
                anonymous = true;
                break;
            }
        }

        if (anonymous){
            return chain.filter(exchange);
        }

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
        // 无法获取token 认证失败
        if (StrUtil.isEmpty(token)) {
//            Response result = new Response();
//                     result.failure(BizCodeEnum.ACCOUNT_OR_PWD_ERROR);
            Map map = new HashMap<>();
            map.put("code", 40001);

            byte[] bits = JSONUtil.toJsonStr(map).getBytes(StandardCharsets.UTF_8);
            DataBuffer buffer = response.bufferFactory().wrap(bits);

            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            return response.writeWith(Mono.just(buffer));
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
