package com.wyc21.entity;

import lombok.Data;
import java.util.Date;

@Data
public class Cart {
    private Long cartId;
    private Long userId;
    private String createdUser;
    private Date createdTime;
    private Date modifiedTime;
    private Boolean isCheckedOut = false;
}