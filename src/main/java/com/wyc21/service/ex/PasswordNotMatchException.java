package com.wyc21.service.ex;

// 密码不匹配异常
public class PasswordNotMatchException extends ServiceException {
    public PasswordNotMatchException() {
        super();
    }

    public PasswordNotMatchException(String message) {
        super(message);
    }
    public PasswordNotMatchException(Throwable cause) {
        super(cause);
    }
    public PasswordNotMatchException(String message, Throwable cause) {
        super(message, cause);
    }
    public PasswordNotMatchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
