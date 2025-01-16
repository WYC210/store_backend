package com.wyc21.service.impl;

import com.wyc21.entity.Product;

import com.wyc21.service.ProductService;
import com.wyc21.util.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.wyc21.mapper.ProductMapper;
import java.util.List;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    public PageResult<Product> getProducts(Long categoryId, String keyword, int page, int size) {
        // 计算偏移量
        int offset = (page - 1) * size;
        
        // 查询数据
        List<Product> products = productMapper.findProducts(categoryId, keyword, offset, size);
        int total = productMapper.countProducts(categoryId, keyword);
        
        // 构建分页结果
        PageResult<Product> result = new PageResult<>();
        result.setList(products);
        result.setTotal(total);
        result.setPages((total + size - 1) / size);
        result.setPageNum(page);
        result.setPageSize(size);
        
        return result;
    }
    
    @Override
    public Product getProduct(Long productId) {
        return productMapper.findById(productId);
    }
} 