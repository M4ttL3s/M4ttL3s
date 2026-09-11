package com.distributed.inventory.infrastructure.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockRejectedEvent(
        UUID eventId,
        UUID orderId,
        UUID productId,
        int quantity,
        String reason,
        LocalDateTime timestamp
) {}
