package com.distributed.inventory.infrastructure.messaging.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderItemPayload(
        UUID productId,
        int quantity,
        BigDecimal unitPrice
) {}
