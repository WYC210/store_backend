package com.wyc21.service.ex;

public class InsuffientStockException extends ServiceException {
    public InsuffientStockException() {
        super();
    }

    public InsuffientStockException(String message) {
        super(message);
    }

    public InsuffientStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsuffientStockException(Throwable cause) {
        super(cause);
    }
} 