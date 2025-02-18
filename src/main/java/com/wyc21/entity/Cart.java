package com.wyc21.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Cart extends BaseEntity {
    private String cartId;
    private String userId;
    private Integer isCheckedOut;
    private String createdUser;
    private LocalDateTime createdTime;
}