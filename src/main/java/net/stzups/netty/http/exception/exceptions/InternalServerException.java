package net.stzups.netty.http.exception.exceptions;


import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.netty.http.exception.HttpException;

public class InternalServerException extends HttpException {
    public InternalServerException(String message) {
        super(message);
    }

    public InternalServerException(Throwable cause) {
        super(cause);
    }

    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public HttpResponseStatus responseStatus() {
        return HttpResponseStatus.INTERNAL_SERVER_ERROR;
    }
}
