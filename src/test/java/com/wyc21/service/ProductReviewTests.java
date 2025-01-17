package com.wyc21.service;

import com.wyc21.entity.Product;
import com.wyc21.entity.ProductReview;
import com.wyc21.util.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
public class ProductReviewTests {

    @Autowired
    private ProductService productService;

    @Test
    public void testGetProductsWithReviews() {
        log.info("开始测试获取带评论的商品列表");

        // 获取第一页商品，每页5条
        PageResult<Product> result = productService.getProducts(null, null, 1, 5);

        // 基本验证
        assertNotNull(result, "返回结果不应为空");
        assertNotNull(result.getList(), "商品列表不应为空");
        assertFalse(result.getList().isEmpty(), "商品列表不应为空");

        // 验证每个商品的评论
        result.getList().forEach(product -> {
            log.info("商品: {} (ID: {})", product.getName(), product.getProductId());

            List<ProductReview> reviews = product.getReviews();
            if (reviews != null && !reviews.isEmpty()) {
                log.info("评论数量: {}", reviews.size());

                reviews.forEach(review -> {
                    log.info("  - 用户: {}", review.getUsername());
                    log.info("    评分: {}", review.getRating());
                    log.info("    内容: {}", review.getContent());
                    log.info("    时间: {}", review.getCreatedTime());

                    // 验证评论字段
                    assertNotNull(review.getReviewId(), "评论ID不应为空");
                    assertNotNull(review.getUserId(), "用户ID不应为空");
                    assertNotNull(review.getUsername(), "用户名不应为空");
                    assertNotNull(review.getContent(), "评论内容不应为空");
                    assertNotNull(review.getRating(), "评分不应为空");
                    assertTrue(review.getRating().doubleValue() >= 1.0
                            && review.getRating().doubleValue() <= 5.0,
                            "评分应在1-5之间");
                });
            } else {
                log.info("该商品暂无评论");
            }
            log.info("------------------------");
        });

        log.info("商品评论测试完成");
    }

    @Test
    public void testGetProductReviews() {
        log.info("开始测试获取单个商品的评论");

        // 测试获取 iPhone 15 Pro (ID: 1) 的评论
        Long productId = 1L;
        int limit = 5;
        List<ProductReview> reviews = productService.getProductReviews(productId, limit);

        // 验证评论列表
        assertNotNull(reviews, "评论列表不应为空");
        assertFalse(reviews.isEmpty(), "评论列表不应为空");
        assertTrue(reviews.size() <= limit, "评论数量不应超过限制");

        // 验证每条评论
        reviews.forEach(review -> {
            log.info("评论ID: {}", review.getReviewId());
            log.info("用户: {}", review.getUsername());
            log.info("评分: {}", review.getRating());
            log.info("内容: {}", review.getContent());
            log.info("时间: {}", review.getCreatedTime());
            log.info("------------------------");

            // 验证评论属于正确的商品
            assertEquals(productId, review.getProductId(),
                    "评论应该属于正确的商品");

            // 验证评论的完整性
            assertNotNull(review.getUserId(), "用户ID不应为空");
            assertNotNull(review.getUsername(), "用户名不应为空");
            assertNotNull(review.getContent(), "评论内容不应为空");
            assertNotNull(review.getRating(), "评分不应为空");
            assertTrue(review.getRating().doubleValue() >= 1.0
                    && review.getRating().doubleValue() <= 5.0,
                    "评分应在1-5之间");
        });

        log.info("单个商品评论测试完成");
    }

    @Test
    public void testReviewSorting() {
        log.info("开始测试评论排序");

        // 获取最新的评论
        Long productId = 1L;
        List<ProductReview> reviews = productService.getProductReviews(productId, 10);

        // 验证评论是按时间倒序排列的
        for (int i = 1; i < reviews.size(); i++) {
            assertTrue(
                    reviews.get(i - 1).getCreatedTime().compareTo(
                            reviews.get(i).getCreatedTime()) >= 0,
                    "评论应该按时间倒序排列");
        }

        log.info("评论排序测试完成");
    }
}