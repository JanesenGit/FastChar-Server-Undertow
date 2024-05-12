package com.fastchar.server.undertow;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

import java.util.List;

public class HttpHandlerCollection implements HttpHandler {
    private final List<HttpHandler> handlers;


    public HttpHandlerCollection(List<HttpHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        for (HttpHandler handler : handlers) {
            handler.handleRequest(exchange);
        }
    }


}
