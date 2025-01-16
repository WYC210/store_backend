package com.wyc21.service;

import com.wyc21.entity.Category;
import java.util.List;

public interface CategoryService {
    // 获取分类树
    List<Category> getCategoryTree();
}