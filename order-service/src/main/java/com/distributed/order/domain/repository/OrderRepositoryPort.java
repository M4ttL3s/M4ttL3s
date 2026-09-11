package com.distributed.order.domain.repository;

import com.distributed.order.domain.model.Order;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepositoryPort {

    Order save(Order order);

    Optional<Order> findById(UUID id);

    boolean existsById(UUID id);
}
