package com.distributed.inventory.infrastructure.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record StockReservedEvent(
        UUID eventId,
        UUID orderId,
        UUID productId,
        int quantity,
        LocalDateTime timestamp
) {}
