package com.wyc21.controller;

import com.wyc21.entity.Product;
import com.wyc21.entity.ProductReview;
import com.wyc21.entity.PageResult;
import com.wyc21.service.ProductService;
import com.wyc21.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import com.wyc21.mapper.ProductMapper;
import com.wyc21.service.ex.ProductNotFoundException;

@RestController
@RequestMapping("/products")
public class ProductController extends BaseController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ResourceLoader resourceLoader; // 用于加载资源

    @GetMapping
    public PageResult<Product> getProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        return productService.getProducts(categoryId, keyword, pageNum, pageSize, imageUrl);
    }

    @GetMapping("/{id}")
    public JsonResult<Map<String, Object>> getProduct(@PathVariable Long id) {
        // 获取商品基本信息
        Product product = productService.getProduct(String.valueOf(id));
        // 获取商品所有图片
        List<String> images = productMapper.findProductImages(id);

        if (product != null) {
            // 补充图片的完整访问路径
            String imageUrl = product.getImageUrl();
            if (imageUrl != null && !imageUrl.startsWith("http")) {
                product.setImageUrl("http://localhost:8088/products" + imageUrl);
            }

            // 处理所有图片的URL
            List<String> fullImageUrls = images.stream()
                    .map(img -> {
                        if (img != null && !img.startsWith("http")) {
                            return "http://localhost:8088/products" + img;
                        }
                        return img;
                    })
                    .collect(Collectors.toList());

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("product", product);
            result.put("images", fullImageUrls);

            return new JsonResult<>(OK, result);
        }

        throw new ProductNotFoundException("商品不存在");
    }

    @GetMapping("/{productId}/reviews")
    public JsonResult<List<ProductReview>> getProductReviews(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "5") int limit) {
        List<ProductReview> reviews = productService.getProductReviews(productId, limit);
        return new JsonResult<>(OK, reviews);
    }

    @GetMapping("/images/{imageName:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String imageName) {
        try {
            Resource resource = resourceLoader.getResource("classpath:images/" + imageName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg") // 添加正确的Content-Type
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}