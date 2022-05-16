package io.nerv.config;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * 自定义返回结果：没有登录或token过期时
 */
@Component
public class RestAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    private static final Pattern CALLBACK_PARAM_PATTERN = Pattern.compile("[0-9A-Za-z_\\.]*");

    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException e) {
        ServerHttpResponse response = exchange.getResponse();

        response.setStatusCode(HttpStatus.OK);


        DataBuffer buffer = response.bufferFactory().wrap("notoken body".getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    protected boolean isValidJsonpQueryParam(String value) {
        return CALLBACK_PARAM_PATTERN.matcher(value).matches();
    }
}