package com.wyc21.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class User implements Serializable {
    private Long uid;
    private String username;
    private String password;
    private String power; // 用户权限
    private String phone;
    private String email;
    private Integer gender;
    private String avatar; // 头像URL
    private Integer isDelete; // 是否删除：0-未删除，1-已删除
    private String createdUser;
    private LocalDateTime createdTime;
    private String modifiedUser;
    private LocalDateTime modifiedTime;
    private String token; // 用于存储JWT token
}
