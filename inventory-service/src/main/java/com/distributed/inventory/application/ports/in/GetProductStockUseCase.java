package com.distributed.inventory.application.ports.in;

import com.distributed.inventory.application.dto.ProductStockResponse;

import java.util.UUID;

public interface GetProductStockUseCase {
    ProductStockResponse getProductStock(UUID productId);
}
