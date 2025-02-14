package com.wyc21.service;

import com.wyc21.entity.Product;
import com.wyc21.entity.ProductReview;
import com.wyc21.entity.PageResult;
import java.util.List;

public interface ProductService {
    // 分页获取商品列表
    PageResult<Product> getProducts(Long categoryId, String keyword, int pageNum, int pageSize,String imageUrl);
    
    // 获取商品详情
    Product getProduct(Long productId);
    
    // 获取商品评论
    List<ProductReview> getProductReviews(Long productId, int limit);
} 