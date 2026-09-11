package com.distributed.order.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItem {

    private final UUID id;
    private final UUID productId;
    private final int quantity;
    private final BigDecimal unitPrice;

    public OrderItem(UUID id, UUID productId, int quantity, BigDecimal unitPrice) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Unit price must be non-negative");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getSubtotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
