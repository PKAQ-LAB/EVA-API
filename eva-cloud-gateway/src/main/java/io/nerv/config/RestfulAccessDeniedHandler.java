package io.nerv.config;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/**
 * 自定义返回结果：没有权限访问时
 */
@Component
public class RestfulAccessDeniedHandler implements ServerAccessDeniedHandler {
    private static final Pattern CALLBACK_PARAM_PATTERN = Pattern.compile("[0-9A-Za-z_\\.]*");

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        ServerHttpResponse response = exchange.getResponse();


        DataBuffer buffer = response.bufferFactory().wrap("body".getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    protected boolean isValidJsonpQueryParam(String value) {
        return CALLBACK_PARAM_PATTERN.matcher(value).matches();
    }
}