package com.distributed.inventory.infrastructure.rest.controller;

import com.distributed.inventory.application.dto.CreateProductCommand;
import com.distributed.inventory.application.dto.ProductStockResponse;
import com.distributed.inventory.application.ports.in.CreateProductUseCase;
import com.distributed.inventory.application.ports.in.GetProductStockUseCase;
import com.distributed.inventory.infrastructure.rest.dto.CreateProductRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final GetProductStockUseCase getProductStockUseCase;

    public ProductController(CreateProductUseCase createProductUseCase,
                             GetProductStockUseCase getProductStockUseCase) {
        this.createProductUseCase = createProductUseCase;
        this.getProductStockUseCase = getProductStockUseCase;
    }

    @PostMapping
    public ResponseEntity<ProductStockResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        CreateProductCommand command = new CreateProductCommand(request.productId(), request.initialStock());
        ProductStockResponse response = createProductUseCase.createProduct(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{productId}/stock")
    public ResponseEntity<ProductStockResponse> getProductStock(@PathVariable UUID productId) {
        ProductStockResponse response = getProductStockUseCase.getProductStock(productId);
        return ResponseEntity.ok(response);
    }
}
