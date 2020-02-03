package io.nerv.server.undertow;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConditionalOnProperty(prefix = "spring.profiles", name = "active", havingValue = "prod")
public class GracefulShutdownUndertowWrapper implements HandlerWrapper {
    private GracefulShutdownHandler gracefulShutdownHandler;

    @Override
    public HttpHandler wrap(HttpHandler handler) {
        if(gracefulShutdownHandler == null) {
            this.gracefulShutdownHandler = new GracefulShutdownHandler(handler);
        }
        return gracefulShutdownHandler;
    }

    public GracefulShutdownHandler getGracefulShutdownHandler() {
        return gracefulShutdownHandler;
    }
}