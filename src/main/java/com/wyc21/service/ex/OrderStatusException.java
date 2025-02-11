package com.wyc21.service.ex;

public class OrderStatusException extends ServiceException {
    public OrderStatusException() {
        super();
    }

    public OrderStatusException(String message) {
        super(message);
    }

    public OrderStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderStatusException(Throwable cause) {
        super(cause);
    }
} 