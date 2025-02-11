package com.wyc21.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
// 实体类的基类
@Data
public class BaseEntity implements Serializable {
    private String createdUser; 
    private LocalDateTime createdTime; 
    private String modifiedUser; 
    private LocalDateTime modifiedTime;
    
    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getModifiedTime() {
        return modifiedTime;
    }

    public void setModifiedTime(LocalDateTime modifiedTime) {
        this.modifiedTime = modifiedTime;
    }
}
