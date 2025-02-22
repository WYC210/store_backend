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

        productService.createProduct(product);

        verify(productMapper, times(1)).insertProduct(product);
    }

    @Test
    public void testCreateProduct() {
        // Arrange
        Product product = new Product();
        product.setProductId("3");
        product.setName("艾莉");
        product.setPrice(BigDecimal.valueOf(6999.00));
        product.setStock(100);
        product.setCategoryId("11");
        product.setDescription("最新款iPhone手机");
        product.setBrand("Apple");

        // Act
        productService.createProduct(product);

        // Assert
        verify(productMapper, times(1)).insertProduct(product);
    }

    @Test
    public void testUpdateProduct() {
        // Arrange
        Product existingProduct = new Product();
        existingProduct.setProductId("1");
        existingProduct.setName("iPhone 14");
        existingProduct.setPrice(BigDecimal.valueOf(6999.00));
        existingProduct.setStock(100);
        existingProduct.setCategoryId("11");
        existingProduct.setDescription("最新款iPhone手机");
        existingProduct.setBrand("Apple");

        when(productMapper.findById("1")).thenReturn(existingProduct);

        // Update product details
        existingProduct.setPrice(BigDecimal.valueOf(6499.00)); // Update price

        // Act
        productService.updateProduct(existingProduct);

        // Assert
        verify(productMapper, times(1)).updateProduct(existingProduct);
    }

    @Test
    public void testDeactivateProduct() {
        // Arrange
        Product existingProduct = new Product();
        existingProduct.setProductId("1");
        existingProduct.setIsActive(1); // Initially active

        when(productMapper.findById("1")).thenReturn(existingProduct);

        // Act
        productService.deactivateProduct("1");

        // Assert
          // Check if the product is now inactive
        verify(productMapper, times(1)).updateProduct(existingProduct);
    }
}