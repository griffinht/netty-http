package net.stzups.netty.http.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import net.stzups.netty.http.exception.HttpException;

public abstract class HttpHandler {
    private final String route;

    protected HttpHandler(String route) {
        this.route = route;
    }

    public String getRoute() {
        return route;
    }

    /**
     * true if the request was handled, or false if it should be passed down
     */
    public abstract boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException;
}
