package com.distributed.order.infrastructure.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateOrderItemRequest(
        @NotNull(message = "Product ID is mandatory")
        UUID productId,

        @Min(value = 1, message = "Quantity must be at least 1")
        int quantity,

        @NotNull(message = "Unit price is mandatory")
        @DecimalMin(value = "0.0", inclusive = false, message = "Unit price must be positive")
        BigDecimal unitPrice
) {}
