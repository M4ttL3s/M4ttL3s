package com.distributed.inventory.infrastructure.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateProductRequest(
        @NotNull(message = "Product ID cannot be null")
        UUID productId,

        @Min(value = 0, message = "Initial stock cannot be negative")
        int initialStock
) {}
