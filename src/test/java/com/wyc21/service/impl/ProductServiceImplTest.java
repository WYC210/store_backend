package com.wyc21.service.impl;

import com.wyc21.entity.Product;
import com.wyc21.mapper.ProductMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;
import java.math.BigDecimal;

import static org.mockito.Mockito.*;

public class ProductServiceImplTest {

    @InjectMocks
    private ProductServiceImpl productService;

    @Mock
    private ProductMapper productMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testPublishProduct() {
        Product product = new Product();
        product.setName("Test Product");
        product.setPrice(new BigDecimal("99.99"));
        product.setStock(100);

        productService.publishProduct(product);

        verify(productMapper, times(1)).insert(product);
    }

    // @Test
    // public void testPublishProduct_NullProduct() {
    // Exception exception = assertThrows(IllegalArgumentException.class, () -> {
    // productService.publishProduct(null);
    // });
    // assertEquals("商品信息不能为空", exception.getMessage());
    // }

    // @Test
    // public void testPublishProduct_InvalidProduct() {
    // Product product = new Product();
    // Exception exception = assertThrows(IllegalArgumentException.class, () -> {
    // productService.publishProduct(product);
    // });
    // assertEquals("商品名称、价格和库存不能为空", exception.getMessage());
    // }
}