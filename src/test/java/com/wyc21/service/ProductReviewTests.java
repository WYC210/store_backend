package com.wyc21.service;

import com.wyc21.entity.Product;
import com.wyc21.entity.ProductReview;
import com.wyc21.entity.Category;
import com.wyc21.mapper.ProductMapper;
import com.wyc21.mapper.CategoryMapper;
import com.wyc21.entity.PageResult;
import com.wyc21.util.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
public class ProductReviewTests {

    @Autowired
    private ProductService productService;

    @Autowired
    private DatabaseInitService databaseInitService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private SnowflakeIdGenerator idGenerator;

    @BeforeEach
    void setUp() {
        // 1. 初始化数据库
        databaseInitService.initializeDatabase();

        // 2. 创建测试分类
        Category category = createTestCategory("电子产品");

        // 3. 创建测试商品（包含评分和评论信息）
        createTestProduct("iPhone 15", new BigDecimal("6999.00"), category.getCategoryId(),
                new BigDecimal("4.5"), 100, "非常好用的手机");
        createTestProduct("MacBook Pro", new BigDecimal("12999.00"), category.getCategoryId(),
                new BigDecimal("4.8"), 50, "性能强劲的笔记本");
    }

    private Category createTestCategory(String name) {
        Category category = new Category();
        category.setCategoryId(idGenerator.nextId());
        category.setName(name);
        categoryMapper.insert(category);
        return category;
    }

    private Product createTestProduct(String name, BigDecimal price, Long categoryId,
            BigDecimal rating, Integer reviewCount, String description) {
        Product product = new Product();
        product.setProductId(idGenerator.nextId());
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(100);
        product.setCategoryId(categoryId);
        product.setBrand("Apple");
        product.setRating(rating);
        product.setReviewCount(reviewCount);
        product.setImageUrl("http://example.com/images/" + name.toLowerCase().replace(" ", "-") + ".jpg");
        product.setIsActive(1);

        productMapper.insert(product);
        return product;
    }

    @Test
    public void testGetProductsWithReviews() {
        log.info("开始测试获取带评论的商品列表");

        PageResult<Product> result = productService.getProducts(null, null, 1, 5);

        assertNotNull(result, "返回结果不应为空");
        assertNotNull(result.getList(), "商品列表不应为空");
        assertFalse(result.getList().isEmpty(), "商品列表不应为空");
        assertTrue(result.getTotal() > 0, "总记录数应大于0");
        assertEquals(1, result.getPageNum(), "当前页码应为1");
        assertEquals(5, result.getPageSize(), "每页大小应为5");

        // 验证商品评论信息
        result.getList().forEach(product -> {
            log.info("商品: {} (ID: {})", product.getName(), product.getProductId());
            log.info("评分: {}", product.getRating());
            log.info("评论数: {}", product.getReviewCount());
            log.info("描述: {}", product.getDescription());
            log.info("------------------------");

            assertNotNull(product.getRating(), "评分不应为空");
            assertTrue(
                    product.getRating().compareTo(BigDecimal.ONE) >= 0 &&
                            product.getRating().compareTo(new BigDecimal("5")) <= 0,
                    "评分应在1-5之间");
            assertNotNull(product.getReviewCount(), "评论数不应为空");
            assertTrue(product.getReviewCount() >= 0, "评论数应该大于等于0");
        });

        log.info("商品评论测试完成");
    }

    @Test
    public void testGetProductReviews() {
        log.info("开始测试获取单个商品的评论信息");

        // 使用findProducts替代findAll
        List<Product> products = productMapper.findProducts(null, null, 0, 1);
        assertFalse(products.isEmpty(), "应该能找到测试商品");
        Long productId = products.get(0).getProductId();

        List<ProductReview> reviews = productService.getProductReviews(productId, 5);

        assertNotNull(reviews, "评论信息不应为空");
        assertFalse(reviews.isEmpty(), "评论信息不应为空");

        reviews.forEach(review -> {
            log.info("商品名称: {}", review.getName());
            log.info("评分: {}", review.getRating());
            log.info("评论数: {}", review.getReviewCount());
            log.info("描述: {}", review.getDescription());
            log.info("------------------------");

            assertNotNull(review.getRating(), "评分不应为空");
            assertTrue(
                    review.getRating().compareTo(BigDecimal.ONE) >= 0 &&
                            review.getRating().compareTo(new BigDecimal("5")) <= 0,
                    "评分应在1-5之间");
            assertNotNull(review.getReviewCount(), "评论数不应为空");
            assertTrue(review.getReviewCount() >= 0, "评论数应该大于等于0");
        });

        log.info("单个商品评论信息测试完成");
    }

    @Test
    public void testReviewSorting() {
        log.info("开始测试评论排序");

        // 使用findProducts替代findAll
        List<Product> products = productMapper.findProducts(null, null, 0, 1);
        assertFalse(products.isEmpty(), "应该能找到测试商品");
        Long productId = products.get(0).getProductId();

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