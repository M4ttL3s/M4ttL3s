package com.distributed.inventory.application.ports.in;

import com.distributed.inventory.application.dto.CreateProductCommand;
import com.distributed.inventory.application.dto.ProductStockResponse;

public interface CreateProductUseCase {
    ProductStockResponse createProduct(CreateProductCommand command);
}
