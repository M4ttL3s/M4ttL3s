package com.distributed.order.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        String customerId,
        BigDecimal totalAmount,
        List<OrderItemPayload> items,
        LocalDateTime createdAt
) {}
