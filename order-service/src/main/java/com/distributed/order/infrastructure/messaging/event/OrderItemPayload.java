package com.distributed.order.infrastructure.messaging.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemPayload(
        UUID productId,
        int quantity,
        BigDecimal unitPrice
) {}
