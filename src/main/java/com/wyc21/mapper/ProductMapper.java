package com.wyc21.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.wyc21.entity.Product;

import java.util.List;

@Mapper
public interface ProductMapper {
    // 获取商品列表（支持分页和条件查询）
    List<Product> findProducts(@Param("categoryId") Long categoryId,
                             @Param("keyword") String keyword,
                             @Param("offset") int offset,
                             @Param("limit") int limit);
    
    // 获取商品总数
    int countProducts(@Param("categoryId") Long categoryId,
                     @Param("keyword") String keyword);
    
    // 获取商品详情（包括图片）
    Product findById(Long productId);
} 