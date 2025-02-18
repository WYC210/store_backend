package com.wyc21.mapper;

import com.wyc21.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    /**
     * 插入用户数据
     * 
     * @param user 用户数据
     * @return 受影响的行数
     */
    Integer insert(User user);

    /**
     * 根据用户名查询用户数据
     * 
     * @param username 用户名
     * @return 匹配的用户数据，如果没有匹配的数据，则返回null
     */
    User findByUsername(String username);

    /**
     * 根据用户id查询用户数据
     * 
     * @param uid 用户id
     * @return 用户数据
     */
    User findByUid(String uid);

    /**
     * 更新用户信息
     * 
     * @param user 用户数据
     * @return 受影响的行数
     */
    Integer updateInfo(User user);

    /**
     * 更新用户密码
     * 
     * @param user 用户数据
     * @return 受影响的行数
     */
    Integer updatePassword(User user);
}