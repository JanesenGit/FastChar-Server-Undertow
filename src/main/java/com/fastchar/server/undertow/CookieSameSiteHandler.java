package com.fastchar.server.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;

public class CookieSameSiteHandler implements HttpHandler {
    private final FastUndertowConfig undertowConfig;

    public CookieSameSiteHandler(FastUndertowConfig undertowConfig) {
        this.undertowConfig = undertowConfig;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (undertowConfig.getCookieSameSite() != null) {
            exchange.addResponseCommitListener(exchange1 -> {
                for (Cookie cookie : exchange1.responseCookies()) {
                    cookie.setSameSiteMode(undertowConfig.getCookieSameSite().attributeValue());
                }
            });
        }
    }
}
