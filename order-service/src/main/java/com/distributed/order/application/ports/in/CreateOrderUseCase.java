package com.distributed.order.application.ports.in;

import com.distributed.order.application.dto.CreateOrderCommand;
import com.distributed.order.application.dto.OrderResponse;

public interface CreateOrderUseCase {
    OrderResponse createOrder(CreateOrderCommand command);
}
