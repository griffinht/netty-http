package net.stzups.netty.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.*;
import net.stzups.netty.TestLog;
import net.stzups.netty.http.exception.HttpException;
import net.stzups.netty.http.exception.exceptions.BadRequestException;
import net.stzups.netty.http.handler.HttpHandler;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.logging.Level;

import static net.stzups.netty.http.HttpUtils.send;

@ChannelHandler.Sharable
public class HttpServerHandler extends MessageToMessageDecoder<FullHttpRequest> {
    private final Queue<HttpHandler> handlers = new ArrayDeque<>();

    public HttpServerHandler addLast(HttpHandler... handlers) {
        this.handlers.addAll(Arrays.asList(handlers));
        return this;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) {
        try {
            if (request.decoderResult().isFailure())
                throw new BadRequestException("Decoding request resulted in " + request.decoderResult());

            for (HttpHandler handler : handlers) {
                if (request.uri().startsWith(handler.getRoute()) && handler.handle(ctx, request)) {
                    return;
                }
            }

            TestLog.getLogger(ctx).warning("Reached end of http handler pipeline, serving default 200 OK");
            HttpUtils.send(ctx, request, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK));
        } catch (IndexOutOfBoundsException e) {
            TestLog.getLogger(ctx).log(Level.WARNING, "Exception while handling HTTP request", e);
            send(ctx, request, new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
        } catch (HttpException e) {
            if (e.getCause() != null) {
                TestLog.getLogger(ctx).log(Level.INFO, "Exception while handling HTTP request", e);
            } else {
                TestLog.getLogger(ctx).log(Level.INFO, e.getClass().getSimpleName() + ": " + e.getLocalizedMessage()); //non verbose log for simple exceptions
            }
            send(ctx, request, new DefaultHttpResponse(HttpVersion.HTTP_1_1, e.responseStatus()));
        }
    }
}