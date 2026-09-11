package com.distributed.order.application.dto;

import com.distributed.order.domain.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String customerId,
        OrderStatus status,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
