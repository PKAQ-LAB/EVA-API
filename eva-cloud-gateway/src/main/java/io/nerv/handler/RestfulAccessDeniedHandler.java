package io.nerv.handler;

import io.nerv.core.enums.BizCodeEnum;
import io.nerv.core.mvc.vo.Response;
import io.nerv.core.util.json.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * 自定义返回结果：没有权限访问时
 */
@Slf4j
@Component
public class RestfulAccessDeniedHandler implements ServerAccessDeniedHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {

        ServerHttpRequest request = exchange.getRequest();

        return exchange.getPrincipal()
                .doOnNext(principal -> log.info("用户:[{}]没有访问:[{}]的权限.", principal.getName(), request.getURI()))
                .flatMap(principal -> {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.OK);
                    String body = JsonUtil.toJson(
                            new Response()
                                    .failure(BizCodeEnum.PERMISSION_DENY));

                    DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
                    return response.writeWith(Mono.just(buffer))
                            .doOnError(error -> DataBufferUtils.release(buffer));
                });
    }
}