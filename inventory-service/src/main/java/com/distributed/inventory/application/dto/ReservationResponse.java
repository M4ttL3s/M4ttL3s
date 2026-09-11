package com.distributed.inventory.application.dto;

import com.distributed.inventory.domain.model.ReservationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReservationResponse(
        UUID id,
        UUID orderId,
        UUID productId,
        int quantity,
        ReservationStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
