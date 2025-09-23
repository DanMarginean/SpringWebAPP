package com.example.onlineshop.Service;

import com.example.onlineshop.Entity.Product;
import com.example.onlineshop.Repository.ProductRepository;
import com.example.onlineshop.Util.ProductMapper;
import com.example.onlineshop.dto.ProductRequestDto;
import com.example.onlineshop.dto.ProductResponseDto;
import com.example.onlineshop.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository,ProductMapper mapper){
        this.repository = repository;
        this.mapper = mapper;
    }

    public ProductResponseDto createProduct(ProductRequestDto request){
        Product product = mapper.toEntity(request);
        Product saved = repository.save(product);
        return mapper.toResponse(saved);
    }

    public List<ProductResponseDto> getAllProducts(){
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    public ProductResponseDto getProductById(Long id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));
    }

    public ProductResponseDto updateProduct(Long id, ProductRequestDto request){
        Product product = repository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found"));

        mapper.updateEntity(product,request);
        return mapper.toResponse(repository.save(product));

    }

    public void deleteProduct(Long id){
        repository.deleteById(id);
    }
}
