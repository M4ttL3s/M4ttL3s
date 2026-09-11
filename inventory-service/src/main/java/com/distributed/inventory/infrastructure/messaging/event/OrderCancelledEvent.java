package com.distributed.inventory.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderCancelledEvent(
        UUID eventId,
        UUID orderId,
        Object cancelledAt
) {}
