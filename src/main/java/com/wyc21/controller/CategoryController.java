package com.wyc21.controller;

import com.wyc21.entity.Category;
import com.wyc21.service.CategoryService;
import com.wyc21.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController extends BaseController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public JsonResult<List<Category>> getCategories() {
        List<Category> categories = categoryService.getCategoryTree();
        return new JsonResult<>(OK, categories);
    }
}