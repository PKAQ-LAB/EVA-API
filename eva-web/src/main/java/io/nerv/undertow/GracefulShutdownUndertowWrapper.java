package io.nerv.undertow;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.handlers.GracefulShutdownHandler;
import org.springframework.stereotype.Component;

@Component
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