package com.wyc21.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.wyc21.entity.Product;
import com.wyc21.entity.ProductReview;

import java.util.List;

@Mapper
public interface ProductMapper {
        // 获取商品列表（支持分页和条件查询）
        List<Product> findProducts(@Param("categoryId") Long categoryId,
                        @Param("keyword") String keyword,
                        @Param("offset") int offset,
                        @Param("limit") int limit,
                        @Param("imageUrl") String imageUrl);

        // 获取所有商品（按创建时间倒序）
        List<Product> findAll();

        // 获取商品总数
        long countProducts(@Param("categoryId") Long categoryId,
                        @Param("keyword") String keyword,
                        @Param("imageUrl") String imageUrl);

        // 获取商品详情（包括图片）
        Product findById(String id);

        // 获取商品评论
        List<ProductReview> findReviewsByProductId(@Param("productId") Long productId,
                        @Param("limit") int limit);

        void insertProduct(Product product);

        void updateProduct(Product product);

        Product findProductById(String productId);

        // 更新商品库存
        void updateStock(@Param("productId") Long productId, @Param("stock") Integer stock);

        /**
         * 扣减库存
         */
        void decreaseStock(@Param("productId") String productId, @Param("quantity") Integer quantity);

        /**
         * 恢复库存
         */
        void increaseStock(@Param("productId") String productId, @Param("quantity") Integer quantity);

        /**
         * 锁定商品
         */
        Product findByIdForUpdate(Long productId);

        /**
         * 获取商品的所有图片URL
         * 
         * @param productId 商品ID
         * @return 图片URL列表
         */
        List<String> findProductImages(@Param("productId") Long productId);
}