// package com.wyc21.service;

// import com.wyc21.entity.Category;
// import lombok.extern.slf4j.Slf4j;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// @Slf4j
// @SpringBootTest
// public class CategoryServiceTests {

//     @Autowired
//     private CategoryService categoryService;

//     @Test
//     public void testGetCategoryTree() {
//         log.info("开始测试获取分类树");

//         // 获取分类树
//         List<Category> categories = categoryService.getCategoryTree();

//         // 基本验证
//         assertNotNull(categories, "分类列表不应为空");
//         assertFalse(categories.isEmpty(), "分类列表不应为空");

//         // 验证顶级分类
//         categories.forEach(category -> {
//             log.info("顶级分类: {}", category.getName());

//             // 验证顶级分类属性
//             assertNotNull(category.getCategoryId(), "分类ID不应为空");
//             assertNotNull(category.getName(), "分类名称不应为空");
//             assertNull(category.getParentCategoryId(), "顶级分类的父ID应为空");

//             // 验证子分类
//             List<Category> children = category.getChildren();
//             if (children != null && !children.isEmpty()) {
//                 log.info("  子分类数量: {}", children.size());
//                 children.forEach(child -> {
//                     log.info("  - 子分类: {}", child.getName());

//                     // 验证子分类属性
//                     assertNotNull(child.getCategoryId(), "子分类ID不应为空");
//                     assertNotNull(child.getName(), "子分类名称不应为空");
//                     assertEquals(category.getCategoryId(), child.getParentCategoryId(),
//                             "子分类的父ID应该匹配父分类ID");
//                 });
//             }
//         });

//         // 验证具体分类
//         categories.stream()
//                 .filter(c -> "手机数码".equals(c.getName()))
//                 .findFirst()
//                 .ifPresent(category -> {
//                     log.info("验证手机数码分类");
//                     assertEquals(1L, category.getCategoryId(), "手机数码分类ID应为1");

//                     List<Category> children = category.getChildren();
//                     assertNotNull(children, "子分类列表不应为空");
//                     assertFalse(children.isEmpty(), "应该有子分类");

//                     // 验证子分类
//                     assertTrue(children.stream()
//                             .anyMatch(c -> "智能手机".equals(c.getName())),
//                             "应该包含智能手机子分类");
//                 });

//         log.info("分类树测试完成");
//     }

//     @Test
//     public void testCategoryStructure() {
//         log.info("开始测试分类结构");

//         List<Category> categories = categoryService.getCategoryTree();

//         // 验证基本结构
//         assertTrue(categories.size() >= 2, "应至少有2个顶级分类");

//         // 验证每个分类的完整性
//         categories.forEach(this::validateCategory);

//         log.info("分类结构测试完成");
//     }

//     private void validateCategory(Category category) {
//         // 验证基本属性
//         assertNotNull(category.getCategoryId(), "分类ID不能为空");
//         assertNotNull(category.getName(), "分类名称不能为空");

//         // 验证子分类
//         List<Category> children = category.getChildren();
//         if (children != null && !children.isEmpty()) {
//             children.forEach(child -> {
//                 // 验证父子关系
//                 assertNotNull(child.getParentCategoryId(), "子分类的父ID不能为空");
//                 assertEquals(category.getCategoryId(), child.getParentCategoryId(),
//                         "子分类的父ID必须匹配父分类ID");

//                 // 递归验证子分类
//                 validateCategory(child);
//             });
//         }
//     }
// }