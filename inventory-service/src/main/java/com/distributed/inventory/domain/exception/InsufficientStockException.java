package com.distributed.inventory.domain.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {

    private final UUID productId;
    private final int requestedQuantity;

    public InsufficientStockException(UUID productId, int requestedQuantity) {
        super("Insufficient stock for product: " + productId + ", requested: " + requestedQuantity);
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }
}
