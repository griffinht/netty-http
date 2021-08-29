package net.stzups.netty.http.handler.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import net.stzups.netty.http.exception.HttpException;
import net.stzups.netty.http.exception.exceptions.UnauthorizedException;
import net.stzups.netty.http.handler.HttpHandler;

public class OriginHandler extends HttpHandler {
    private interface Config {
        String getOrigin();
    }

    private final String origin;

    public OriginHandler(Config config) {
        super("/");
        this.origin = config.getOrigin();
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        String origin = request.headers().get(HttpHeaderNames.ORIGIN);
        if (origin == null || !origin.equals(this.origin)) {
            throw new UnauthorizedException("Unknown origin " + origin + ", should have been " + this.origin);
        }

        return false;
    }
}
