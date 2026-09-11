package com.distributed.inventory.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCreatedEvent(
        UUID eventId,
        UUID orderId,
        String customerId,
        BigDecimal totalAmount,
        List<OrderItemPayload> items,
        Object createdAt
) {}
