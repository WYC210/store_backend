package com.wyc21.mapper;

import com.wyc21.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CategoryMapper {
    // 插入分类
    void insert(Category category);

    // 根据ID查询分类
    Category findById(Long categoryId);

    // 查询所有分类
    List<Category> findAll();

    // 查询顶级分类
    List<Category> findRootCategories();

    // 查询子分类
    List<Category> findChildCategories(Long parentId);

    // 更新分类
    void update(Category category);

    // 删除分类
    void delete(Long categoryId);
}