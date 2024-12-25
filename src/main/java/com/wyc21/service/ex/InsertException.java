package com.wyc21.service.ex;
// 插入数据异常
public class InsertException extends ServiceException {
    public InsertException() {
        super();
    }

    public InsertException(String message) {
        super(message);
    }
    
    public InsertException(Throwable cause) {
        super(cause);
    }
    public InsertException(String message, Throwable cause) {
        super(message, cause);
    }
    public InsertException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
