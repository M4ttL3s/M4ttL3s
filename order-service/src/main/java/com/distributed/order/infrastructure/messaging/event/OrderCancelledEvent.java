package com.distributed.order.infrastructure.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderCancelledEvent(
        UUID eventId,
        UUID orderId,
        LocalDateTime cancelledAt
) {}
