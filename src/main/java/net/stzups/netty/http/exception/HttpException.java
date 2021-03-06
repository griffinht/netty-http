package net.stzups.netty.http.exception;

import io.netty.handler.codec.http.HttpResponseStatus;

public abstract class HttpException extends Exception {
    protected HttpException(String message) {
        super(message);
    }

    protected HttpException(Throwable cause) {
        super(cause);
    }

    protected HttpException(String message, Throwable cause) {
        super(message, cause);
    }

    public abstract HttpResponseStatus responseStatus();
}
