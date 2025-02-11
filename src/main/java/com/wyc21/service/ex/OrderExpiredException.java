package com.wyc21.service.ex;

public class OrderExpiredException extends ServiceException {
    public OrderExpiredException() {
        super();
    }

    public OrderExpiredException(String message) {
        super(message);
    }

    public OrderExpiredException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderExpiredException(Throwable cause) {
        super(cause);
    }
} 