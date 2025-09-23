package com.example.onlineshop.Util;

import com.example.onlineshop.Entity.Product;
import com.example.onlineshop.dto.ProductRequestDto;
import com.example.onlineshop.dto.ProductResponseDto;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {
    public Product toEntity(ProductRequestDto request){
        return Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .description(request.getDescription())
                .stockQuantity(request.getStockQuantity())
                .category(request.getCategory())
                .build();

    }

    public ProductResponseDto toResponse(Product product){
        return ProductResponseDto.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .category(product.getCategory())
                .build();
    }

    public void updateEntity(Product product, ProductRequestDto request){
        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setDescription(request.getDescription());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(request.getCategory());
    }
}
