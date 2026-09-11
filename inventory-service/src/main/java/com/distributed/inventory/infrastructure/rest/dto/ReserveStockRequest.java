package com.distributed.inventory.infrastructure.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ReserveStockRequest(
        @NotNull(message = "Order ID is mandatory")
        UUID orderId,

        @NotNull(message = "Product ID is mandatory")
        UUID productId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity
) {}
