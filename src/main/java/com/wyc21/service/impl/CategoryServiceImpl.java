package com.wyc21.service.impl;

import com.wyc21.entity.Category;
import com.wyc21.mapper.CategoryMapper;
import com.wyc21.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

        // 按照父ID分组
        Map<String, List<Category>> parentIdMap = allCategories.stream()
                .collect(Collectors.groupingBy(
                        category -> category.getParentId() != null ? category.getParentId() : "root"));

        // 获取顶级分类
        List<Category> rootCategories = allCategories.stream()
                .filter(category -> category.getParentId() == null)
                .collect(Collectors.toList());

        // 递归设置子分类
        rootCategories.forEach(category -> {
            setChildren(category, parentIdMap);
        });

        return rootCategories;
    }

    private void setChildren(Category parent, Map<String, List<Category>> parentIdMap) {
        List<Category> children = parentIdMap.getOrDefault(parent.getCategoryId(), new ArrayList<>());
        if (!children.isEmpty()) {
            parent.setChildren(children);
            // 递归设置子分类的子分类
            children.forEach(child -> setChildren(child, parentIdMap));
        }
    }
}