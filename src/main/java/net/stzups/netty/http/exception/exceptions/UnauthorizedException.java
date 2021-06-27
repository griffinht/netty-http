package net.stzups.netty.http.exception.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.netty.http.exception.HttpException;

public class UnauthorizedException extends HttpException {
    public UnauthorizedException(String message) {
        super(message);
    }

    public UnauthorizedException(Throwable cause) {
        super(cause);
    }

    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpResponseStatus responseStatus() {
        return HttpResponseStatus.UNAUTHORIZED;
    }
}
