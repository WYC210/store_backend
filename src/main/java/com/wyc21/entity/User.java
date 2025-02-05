package com.wyc21.entity;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends BaseEntity implements Serializable {
    private Long uid;
    private String username;
    private String password;
    private String power;
    private String phone;
    private String email;
    private Integer gender;
    private String avatar;
    private Boolean isDelete;
    private String createdUser;
    private Date createdTime;
    private String modifiedUser;
    private Date modifiedTime;
    private String token;
}
