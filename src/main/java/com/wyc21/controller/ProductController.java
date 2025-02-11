package com.wyc21.controller;

import com.wyc21.entity.Product;
import com.wyc21.entity.ProductReview;
import com.wyc21.entity.PageResult;
import com.wyc21.service.ProductService;
import com.wyc21.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController extends BaseController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public PageResult<Product> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return productService.getProducts(categoryId, keyword, pageNum, pageSize);
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @GetMapping("/{productId}/reviews")
    public JsonResult<List<ProductReview>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "5") int limit) {
        List<ProductReview> reviews = productService.getProductReviews(productId, limit);
        return new JsonResult<>(OK, reviews);
    }
}