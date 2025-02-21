package com.wyc21.service;

import com.wyc21.entity.Product;
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
import java.util.List;

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

    @Test
    public void testGetProductsWithoutFilters() {
        log.info("测试：获取商品列表 - 无筛选条件");

        PageResult<Product> result = productService.getProducts(null, null, 1, 10, null);

        // 基本验证
        assertNotNull(result, "返回结果不应为空");
        assertNotNull(result.getList(), "商品列表不应为空");
        assertTrue(result.getTotal() >= 0, "总记录数应大于等于0");
        assertTrue(result.getPages() >= 0, "总页数应大于等于0");

        // 分页验证
        assertEquals(1, result.getPageNum(), "当前页码应为1");
        assertEquals(10, result.getPageSize(), "每页大小应为10");

        // 数据完整性验证
        result.getList().forEach(product -> {
            log.info("验证商品: {}", product.getName());
            assertNotNull(product.getProductId(), "商品ID不应为空");
            assertNotNull(product.getName(), "商品名称不应为空");
            assertNotNull(product.getPrice(), "商品价格不应为空");
            assertTrue(product.getPrice().compareTo(BigDecimal.ZERO) > 0, "商品价格应大于0");
            assertTrue(product.getStock() >= 0, "商品库存应大于等于0");
        });
    }

    @Test
    public void testGetProductsByCategory() {
        log.info("测试：获取商品列表 - 按分类筛选");

        Long categoryId = 1L; // 电子产品分类
        PageResult<Product> result = productService.getProducts(categoryId, null, 1, 10, null);

        assertNotNull(result, "返回结果不应为空");
        assertFalse(result.getList().isEmpty(), "商品列表不应为空");

        // 验证所有商品都属于指定分类
        result.getList().forEach(product -> {
            assertEquals(categoryId, product.getCategoryId(),
                    String.format("商品 %s 应属于分类 %d", product.getName(), categoryId));
        });
    }

    @Test
    public void testGetProductsByKeyword() {
        log.info("测试：获取商品列表 - 关键词搜索");

        String keyword = "iPhone";
        PageResult<Product> result = productService.getProducts(null, keyword, 1, 10, null);

        assertNotNull(result, "返回结果不应为空");
        assertFalse(result.getList().isEmpty(), "搜索结果不应为空");

        // 验证搜索结果包含关键词
        result.getList().forEach(product -> {
            boolean containsKeyword = product.getName().toLowerCase().contains(keyword.toLowerCase()) ||
                    (product.getDescription() != null &&
                            product.getDescription().toLowerCase().contains(keyword.toLowerCase()))
                    ||
                    (product.getBrand() != null &&
                            product.getBrand().toLowerCase().contains(keyword.toLowerCase()));

            assertTrue(containsKeyword,
                    String.format("商品 %s 应包含关键词 %s", product.getName(), keyword));
        });
    }

    @Test
    public void testInvalidPageParameters() {
        log.info("测试：获取商品列表 - 无效的分页参数");

        // 测试页码为0
        PageResult<Product> result1 = productService.getProducts(null, null, 0, 10, null);
        assertEquals(1, result1.getPageNum(), "无效页码应被修正为1");

        // 测试页大小为0
        PageResult<Product> result2 = productService.getProducts(null, null, 1, 0, null);
        assertTrue(result2.getPageSize() > 0, "无效页大小应被修正为正数");

        // 测试超大页码
        PageResult<Product> result3 = productService.getProducts(null, null, 999, 10, null);
        assertTrue(result3.getList().isEmpty(), "超出范围的页码应返回空列表");
    }

    @Test
    public void testPublishProduct() {
        log.info("测试：发布商品");

        Product product = new Product();
        product.setName("新商品");
        product.setPrice(new BigDecimal("199.99"));
        product.setStock(50);
        product.setDescription("这是一个新发布的商品");
        product.setImageUrl("/images/new_product.jpg");

        // 调用发布商品的方法
        productService.publishProduct(product);

        // 验证商品是否成功插入
        Product insertedProduct = productMapper.findById(product.getProductId());
        assertNotNull(insertedProduct, "商品应成功插入数据库");
        assertEquals("新商品", insertedProduct.getName(), "商品名称应匹配");
        assertEquals(199.99, insertedProduct.getPrice().doubleValue(), "商品价格应匹配");
        assertEquals(50, insertedProduct.getStock(), "商品库存应匹配");
    }

    @Test
    public void testPublishProduct_NullProduct() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.publishProduct(null);
        });
        assertEquals("商品信息不能为空", exception.getMessage());
    }

    @Test
    public void testPublishProduct_InvalidProduct() {
        Product product = new Product();
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            productService.publishProduct(product);
        });
        assertEquals("商品名称、价格和库存不能为空", exception.getMessage());
    }
}