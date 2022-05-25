package io.nerv.filter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import com.nimbusds.jose.JWSObject;
import io.nerv.core.constant.CommonConstant;
import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.util.RedisUtil;
import io.nerv.properties.EvaConfig;
import io.nerv.util.WebfluxResponseUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.security.auth.login.AccountExpiredException;

/**
 * 将登录用户的JWT转化成用户信息的全局过滤器
 */
@Component
@AllArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    private final EvaConfig evaConfig;

    private final RedisUtil redisUtil;

    //TODO 放到commonconstant中
    private static final String X_CLIENT_TOKEN_USER = "x-client-token-user";
    private final static String X_GATEWAY_HEADER = "x-request";
    private final static String X_GATEWAY_VALUE = "eva-gateway-request";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String authToken;
        String tokenKey = evaConfig.getJwt().getHeader();

        //认证信息从Header 或 请求参数 或cookies 中获取
        ServerHttpRequest serverHttpRequest = exchange.getRequest();
        // from header
        authToken = serverHttpRequest.getHeaders().getFirst(tokenKey);
        //from param
        if (StrUtil.isBlank(authToken)) {
            authToken = (null != serverHttpRequest.getQueryParams() && serverHttpRequest.getQueryParams().size() > 0) ?
                        serverHttpRequest.getQueryParams().getFirst(tokenKey) : null;
        }
        //from cookie
        if (StrUtil.isBlank(authToken)) {
            var cookies = serverHttpRequest.getCookies();
                authToken = (null != cookies && cookies.size() > 0 )?
                                            cookies.getFirst(CommonConstant.ACCESS_TOKEN_KEY).getValue() : null;
        }

        // token为空或未以规范开头直接忽略
        if (StrUtil.isBlank(authToken) || StrUtil.startWithIgnoreCase(authToken, evaConfig.getJwt().getTokenHead())) {
            return chain.filter(exchange);
        }

        //从token中解析用户信息并设置到Header中去
        String realToken = authToken.replace(evaConfig.getJwt().getTokenHead(), "");
        JWT jwtObj = JWTUtil.parseToken(realToken);
        String userStr = jwtObj.getPayload().toString();

//      解析JWT获取jti，以jti为key判断redis的黑名单列表是否存在，存在则拦截访问
//      黑名单token(登出、修改密码)校验
        String jti = jwtObj.getPayload("jti").toString();
        String blockTk = redisUtil.get("JWTBLOCK:"+jti);
        if (StrUtil.isNotBlank(blockTk)) {
            return WebfluxResponseUtil.responseFailed(exchange, BizCodeEnum.LOGIN_EXPIRED.getName());
        }

        // 存在token且不是黑名单，request写入JWT的载体信息
        ServerHttpRequest request = serverHttpRequest.mutate()
                                            .header(X_CLIENT_TOKEN_USER, userStr)
                                            .header(X_GATEWAY_HEADER, X_GATEWAY_VALUE).build();
        exchange = exchange.mutate().request(request).build();

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
