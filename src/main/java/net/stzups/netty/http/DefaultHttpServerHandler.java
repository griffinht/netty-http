package net.stzups.netty.http;

import net.stzups.netty.http.handler.handlers.HealthcheckRequestHandler;
import net.stzups.netty.http.handler.handlers.LogHandler;

public class DefaultHttpServerHandler extends HttpServerHandler {
    public DefaultHttpServerHandler() {
        addLast(new HealthcheckRequestHandler())
                .addLast(new LogHandler());
    }
}
