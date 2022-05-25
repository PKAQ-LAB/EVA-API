package io.nerv.util;

import io.nerv.core.mvc.vo.Response;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;

public class WebfluxResponseUtil {
    /**
     * webflux的response返回json对象
     */
    public static Mono responseWriter(ServerWebExchange exchange, String httpStatus, String msg) {
        Response result = new Response().setData(null).setCode(httpStatus).setMessage(msg);
        return responseWrite(exchange, httpStatus, result);
    }

    public static Mono responseFailed(ServerWebExchange exchange, String msg) {
        Response result = new Response().failure(HttpStatus.INTERNAL_SERVER_ERROR.toString(), msg);
        return responseWrite(exchange,HttpStatus.INTERNAL_SERVER_ERROR.toString() , result);
    }

    public static Mono responseWrite(ServerWebExchange exchange, String httpStatus, Response result) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setAccessControlAllowCredentials(true);
        response.getHeaders().setAccessControlAllowOrigin("*");
        response.setStatusCode(HttpStatus.valueOf(httpStatus));
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBufferFactory dataBufferFactory = response.bufferFactory();
        DataBuffer buffer = dataBufferFactory.wrap(result.getMessage().getBytes(Charset.defaultCharset()));
        return response.writeWith(Mono.just(buffer)).doOnError((error) -> {
            DataBufferUtils.release(buffer);
        });
    }
}