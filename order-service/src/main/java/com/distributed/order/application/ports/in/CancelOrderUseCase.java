package com.distributed.order.application.ports.in;

import com.distributed.order.application.dto.OrderResponse;

import java.util.UUID;

public interface CancelOrderUseCase {
    OrderResponse cancelOrder(UUID orderId);
}
