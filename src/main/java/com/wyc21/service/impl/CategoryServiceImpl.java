package com.wyc21.service.impl;

import com.wyc21.entity.Category;
import com.wyc21.mapper.CategoryMapper;
import com.wyc21.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Category> getCategoryTree() {
        // 获取所有分类
        List<Category> allCategories = categoryMapper.findAll();
       
        // 构建分类树
        Map<Long, List<Category>> childrenMap = allCategories.stream()
                .filter(c -> c.getParentCategoryId() != null)
                .collect(Collectors.groupingBy(Category::getParentCategoryId));

        // 获取顶级分类并设置子分类
        return allCategories.stream()
                .filter(c -> c.getParentCategoryId() == null)
                .peek(c -> c.setChildren(childrenMap.getOrDefault(c.getCategoryId(), List.of())))
                .collect(Collectors.toList());
    }
}