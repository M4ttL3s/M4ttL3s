package com.distributed.inventory.application.dto;

import java.util.UUID;

public record ProductStockResponse(
        UUID productId,
        int stock
) {}
