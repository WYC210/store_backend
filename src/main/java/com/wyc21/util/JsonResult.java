package com.wyc21.util;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter 
// 统一响应结果的类型
public class JsonResult<E> implements Serializable {
    // 状态码
    private Integer state;
    // 状态描述信息
    private String message;
    // 数据
    private E data;

    public JsonResult() {
        
    }

    public JsonResult(Integer state, E data) {
        this.state = state;
        this.data = data;
    }

    public JsonResult(Integer state) {
        this.state = state;
    }

    public JsonResult(Throwable e) {
        this.message = e.getMessage();
    }
}

