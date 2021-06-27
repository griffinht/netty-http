package net.stzups.netty.http.exception.exceptions;

import io.netty.handler.codec.http.HttpResponseStatus;
import net.stzups.netty.http.exception.HttpException;

public class BadRequestException extends HttpException {
   public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
       super(message, cause);
    }

    @Override
    public HttpResponseStatus responseStatus() {
        return HttpResponseStatus.BAD_REQUEST;
    }
}
