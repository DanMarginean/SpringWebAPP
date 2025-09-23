package com.example.onlineshop.Controller;


import com.example.onlineshop.Service.ProductService;
import com.example.onlineshop.dto.ProductRequestDto;
import com.example.onlineshop.dto.ProductResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Operations related to products")

public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service){
        this.service=service;
    }
    @Operation(
            summary = "Create a new product",
            description = "Adds a product with name, price, stock, and category"
    )
    @ApiResponse(responseCode = "201", description = "Product created successfully",
            content = @Content(
                    examples = @ExampleObject(
                            name = "Example Product",
                            value = """
                   {
                     "name": "Laptop",
                     "price": 1299.99,
                     "description": "Gaming laptop with RTX GPU",
                     "stockQuantity": 10,
                     "category": "Electronics"
                   }
                   """
                    )
            ))
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    @PostMapping
    public ResponseEntity<ProductResponseDto> create(@Valid @RequestBody ProductRequestDto request){
        ProductResponseDto created = service.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAll(){
        return ResponseEntity.ok(service.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getById(@PathVariable Long id){
        return ResponseEntity.ok(service.getProductById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDto> update(
            @PathVariable Long id,
            @RequestBody ProductRequestDto request
    ){
        return ResponseEntity.ok(service.updateProduct(id,request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){
        service.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
