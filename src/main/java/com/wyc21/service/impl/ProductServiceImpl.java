package com.wyc21.service.impl;

import com.wyc21.entity.Product;
import com.wyc21.entity.ProductReview;
import com.wyc21.service.ProductService;
import com.wyc21.entity.PageResult;
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
    public PageResult<Product> getProducts(Long categoryId, String keyword, int pageNum, int pageSize,
            String imageUrl) {
        // 验证并修正分页参数
        pageNum = Math.max(1, pageNum); // 页码最小为1
        pageSize = Math.max(1, pageSize); // 每页大小最小为1

        // 计算偏移量
        int offset = (pageNum - 1) * pageSize;

        // 查询数据
        List<Product> products = productMapper.findProducts(categoryId, keyword, offset, pageSize, imageUrl);
        long total = productMapper.countProducts(categoryId, keyword, imageUrl);

        // 返回分页结果
        return new PageResult<>(products, total, pageNum, pageSize);
    }

    @Override
    public Product getProduct(String productId) {
        return productMapper.findById(productId);
    }

    @Override
    public List<ProductReview> getProductReviews(Long productId, int limit) {
        // 调用 mapper 获取评论列表
        return productMapper.findReviewsByProductId(productId, limit);
    }

    @Override
    public void publishProduct(Product product) {
        // 进行必要的验证
        if (product == null) {
            throw new IllegalArgumentException("商品信息不能为空");
        }
        if (product.getName() == null || product.getPrice() == null || product.getStock() == null) {
            throw new IllegalArgumentException("商品名称、价格和库存不能为空");
        }

        // 插入商品到数据库
        productMapper.insert(product);
    }
}