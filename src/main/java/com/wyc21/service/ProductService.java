package com.wyc21.service;

import com.wyc21.entity.Product;
import com.wyc21.util.PageResult;

public interface ProductService {
    // 分页获取商品列表
    PageResult<Product> getProducts(Long categoryId, String keyword, int page, int size);
    
    // 获取商品详情
    Product getProduct(Long productId);
} 