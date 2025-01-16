package com.wyc21.mapper;

import com.wyc21.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CategoryMapper {
    // 获取所有分类
    List<Category> findAll();
    
    // 获取顶级分类
    List<Category> findParentCategories();
    
    // 获取子分类
    List<Category> findChildCategories(Long parentId);
} 