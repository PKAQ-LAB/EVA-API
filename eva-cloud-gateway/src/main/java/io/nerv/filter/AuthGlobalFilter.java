package io.nerv.filter;

import cn.hutool.core.util.StrUtil;
import com.nimbusds.jose.JWSObject;
import io.nerv.core.constant.CommonConstant;
import io.nerv.properties.EvaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 将登录用户的JWT转化成用户信息的全局过滤器
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {
    @Autowired
    private EvaConfig evaConfig;

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

        if (StrUtil.isBlank(authToken)) {
            return chain.filter(exchange);
        }

        try {
            //从token中解析用户信息并设置到Header中去
            String realToken = authToken.replace(evaConfig.getJwt().getTokenHead(), "");
            JWSObject jwsObject = JWSObject.parse(realToken);
            String userStr = jwsObject.getPayload().toString();

//            不是正确的的JWT不做解析处理
//            String token = request.getHeaders().getFirst(SecurityConstants.AUTHORIZATION_KEY);
//            if (StrUtil.isBlank(token) || !StrUtil.startWithIgnoreCase(token, SecurityConstants.JWT_PREFIX)) {
//                return chain.filter(exchange);
//            }
//            // 解析JWT获取jti，以jti为key判断redis的黑名单列表是否存在，存在则拦截访问
//            token = StrUtil.replaceIgnoreCase(token, SecurityConstants.JWT_PREFIX, Strings.EMPTY);
//            String payload = StrUtil.toString(JWSObject.parse(token).getPayload());
//            request = exchange.getRequest().mutate()
//                    .header(SecurityConstants.JWT_PAYLOAD_KEY, URLEncoder.encode(payload, "UTF-8"))
//                    .build();
//            exchange = exchange.mutate().request(request).build();
            // 黑名单token(登出、修改密码)校验
//            JSONObject jsonObject = JSONUtil.parseObj(userStr);
//            String jti = jsonObject.getStr("jti");
//
//            Boolean isBlack = false;
//            if (isBlack) {
//
//            }

            // 存在token且不是黑名单，request写入JWT的载体信息
            ServerHttpRequest request = serverHttpRequest.mutate().header("U_", userStr).build();
            exchange = exchange.mutate().request(request).build();
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
