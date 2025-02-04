package com.wyc21.mapper;

import com.wyc21.entity.ProductReview;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductReviewMapper {
    void insert(ProductReview review);
}