package com.wyc21.service;

import com.wyc21.entity.Product;
import com.wyc21.entity.Category;
import com.wyc21.mapper.ProductMapper;
import com.wyc21.mapper.CategoryMapper;
import com.wyc21.util.PageResult;
import com.wyc21.util.SnowflakeIdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@Transactional
public class ProductServiceTests {

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

        // 3. 创建测试商品
        createTestProduct("iPhone 15", new BigDecimal("6999.00"), category.getCategoryId(), 
                "苹果最新旗舰手机", "Apple", "手机,苹果,iPhone");
        createTestProduct("MacBook Pro", new BigDecimal("12999.00"), category.getCategoryId(),
                "强大的专业笔记本", "Apple", "笔记本,苹果,MacBook");
        createTestProduct("iPad Pro", new BigDecimal("6299.00"), category.getCategoryId(),
                "专业平板电脑", "Apple", "平板,苹果,iPad");
    }

    private Category createTestCategory(String name) {
        Category category = new Category();
        category.setCategoryId(idGenerator.nextId());
        category.setName(name);
        categoryMapper.insert(category);
        return category;
    }

    private Product createTestProduct(String name, BigDecimal price, Long categoryId, 
            String description, String brand, String tags) {
        Product product = new Product();
        product.setProductId(idGenerator.nextId());
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(100);
        product.setCategoryId(categoryId);
        product.setBrand(brand);
        product.setTags(tags);
        product.setRating(new BigDecimal("4.5"));
        product.setReviewCount(50);
        product.setImageUrl("http://example.com/images/" + name.toLowerCase().replace(" ", "-") + ".jpg");
        product.setIsActive(true);
       
        productMapper.insert(product);
        return product;
    }

    @Test
    public void testGetProducts() {
        log.info("开始测试获取商品列表");

        // 测试获取第一页商品，每页10条
        PageResult<Product> result = productService.getProducts(null, null, 1, 10);

        // 验证结果
        assertNotNull(result, "返回结果不应为空");
        assertNotNull(result.getList(), "商品列表不应为空");
        assertTrue(result.getTotal() > 0, "总记录数应大于0");
        assertEquals(1, result.getPageNum(), "当前页码应为1");
        assertEquals(10, result.getPageSize(), "每页大小应为10");

        // 打印第一个商品的信息
        if (!result.getList().isEmpty()) {
            Product firstProduct = result.getList().get(0);
            log.info("第一个商品信息：");
            log.info("ID: {}", firstProduct.getProductId());
            log.info("名称: {}", firstProduct.getName());
            log.info("价格: {}", firstProduct.getPrice());
            log.info("库存: {}", firstProduct.getStock());
        }

        log.info("商品列表测试完成");
    }

    @Test
    public void testGetProductById() {
        log.info("开始测试获取商品详情");

        // 测试获取ID为1的商品
        Long productId = 1L;
        Product product = productService.getProduct(productId);

        // 验证结果
        assertNotNull(product, "商品不应为空");
        assertEquals(productId, product.getProductId(), "商品ID应匹配");
        assertNotNull(product.getName(), "商品名称不应为空");
        assertNotNull(product.getPrice(), "商品价格不应为空");
        assertNotNull(product.getStock(), "商品库存不应为空");

        // 打印商品详细信息
        log.info("商品详情：");
        log.info("ID: {}", product.getProductId());
        log.info("名称: {}", product.getName());
        log.info("描述: {}", product.getDescription());
        log.info("价格: {}", product.getPrice());
        log.info("库存: {}", product.getStock());
        log.info("品牌: {}", product.getBrand());
        log.info("图片: {}", product.getImageUrl());

        log.info("商品详情测试完成");
    }

    @Test
    public void testSearchProducts() {
        log.info("开始测试搜索商品");

        String keyword = "iPhone";
        PageResult<Product> result = productService.getProducts(null, keyword, 1, 10);

        assertNotNull(result, "搜索结果不应为空");
        assertFalse(result.getList().isEmpty(), "搜索结果列表不应为空");

        // 验证搜索结果是否包含关键词
        result.getList().forEach(product -> {
            boolean containsKeyword = product.getName().contains(keyword) ||
                    product.getDescription().contains(keyword) ||
                    product.getBrand().contains(keyword);
            // 移除了tags的检查，因为可能为null

            assertTrue(containsKeyword, "搜索结果应包含关键词");
        });

        log.info("找到 {} 个相关商品", result.getTotal());
        log.info("商品搜索测试完成");
    }
}