package net.stzups.netty.http.handler.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponse;
import net.stzups.netty.TestLog;
import net.stzups.netty.http.exception.HttpException;
import net.stzups.netty.http.handler.HttpHandler;

public class LogHandler extends HttpHandler {
    public LogHandler() {
        super("/");
    }

    @Override
    public boolean handle(ChannelHandlerContext ctx, FullHttpRequest request) throws HttpException {
        TestLog.getLogger(ctx).info(log("user-agent", request.headers().get(HttpHeaderNames.USER_AGENT)) + log("origin", request.headers().get(HttpHeaderNames.ORIGIN)) + log("referer", request.headers().get(HttpHeaderNames.REFERER)) + request.method() + " " + request.uri() + " " + request.protocolVersion());
        return false;
    }

    private static String log(String key, String value) {
        return log(key, value, -1);
    }

    private static String log(String key, String value, int limit) {
        if (value == null) {
            return "";
        } else {
            if (limit >= 0) {
                return key + ": " + value.substring(0, Math.min(value.length(), limit)) + ", ";
            } else {
                return key + ": " + value + ", ";
            }
        }
    }
}
