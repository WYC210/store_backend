package com.wyc21.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Cart {
    private Long cartId;
    private Long userId;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private Boolean isCheckedOut = false;
}