package com.distributed.inventory.application.dto;

import java.util.UUID;

public record ReserveStockCommand(
        UUID orderId,
        UUID productId,
        int quantity
) {}
