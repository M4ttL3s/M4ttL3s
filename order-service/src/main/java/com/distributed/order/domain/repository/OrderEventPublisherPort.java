package com.distributed.order.domain.repository;

import com.distributed.order.domain.model.Order;

import java.util.UUID;

public interface OrderEventPublisherPort {

    void publishOrderCreated(Order order);

    void publishOrderCancelled(UUID orderId);
}
