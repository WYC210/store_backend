package com.wyc21.controller;

import org.springframework.web.bind.annotation.ExceptionHandler;

import com.wyc21.service.ex.ServiceException;
import com.wyc21.util.JsonResult;

import com.wyc21.service.ex.UsernameDuplicatedException;
import com.wyc21.service.ex.InsertException;
import com.wyc21.service.ex.UserNotFoundException;
import com.wyc21.service.ex.PasswordNotMatchException;

// 表示基类
public class BaseController {
    // 表示操作成功的状态码
    public static final int OK = 200;

    // 处理异常
    // 表示当Controller层发生异常时，会调用handleException方法
    @ExceptionHandler(ServiceException.class)
    public JsonResult<Void> handleException(Throwable e) {
        JsonResult<Void> result = new JsonResult<>(e);
        if (e instanceof UsernameDuplicatedException) {
            result.setState(4000);
            result.setMessage("用户名被占用");
        } else if (e instanceof UserNotFoundException) {
            result.setState(5001);
            result.setMessage("用户不存在");
        } else if (e instanceof PasswordNotMatchException) {
            result.setState(5002);
            result.setMessage("密码不匹配");
        } else if (e instanceof InsertException) {
            result.setState(5000);
            result.setMessage("注册时发生未知错误");
        }
        return result;
    }
}

