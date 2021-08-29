package net.stzups.netty.http.handler.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import net.stzups.netty.TestLog;
import net.stzups.netty.http.exception.HttpException;
import net.stzups.netty.http.exception.exceptions.NotFoundException;
import net.stzups.netty.http.handler.HttpHandler;

import java.net.InetSocketAddress;

import static net.stzups.netty.http.HttpUtils.send;

public class HealthcheckRequestHandler extends HttpHandler {
    private boolean done = false;

    public HealthcheckRequestHandler() {
        super("/healthcheck");
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        if (request.uri().equals("/healthcheck")) {
            if (!((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().isLoopbackAddress()) {
                throw new NotFoundException("Healthcheck request from address which is not a loopback address");
            } else {
                send(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
                if (!done) {
                    done = true;
                    TestLog.getLogger(ctx).info("Good healthcheck request, further requests will be muted");
                }
                return true;
            }
        }

        return false;
    }
}
