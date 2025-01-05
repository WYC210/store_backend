package com.wyc21.entity;

import java.io.Serializable;

import lombok.Data;

@Data
public class User extends BaseEntity implements Serializable {
    private Integer uid; 
    private String username; 
    private String password; 
    private String power; 
    private String phone; 
    private String email; 
    private Integer gender; 
    private String avatar; 
    private Integer isDelete;
    
    private String token; 
  
}
