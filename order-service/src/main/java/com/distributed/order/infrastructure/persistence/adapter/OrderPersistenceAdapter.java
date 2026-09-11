package com.distributed.order.infrastructure.persistence.adapter;

import com.distributed.order.domain.model.Order;
import com.distributed.order.domain.model.OrderItem;
import com.distributed.order.domain.repository.OrderRepositoryPort;
import com.distributed.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.distributed.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.distributed.order.infrastructure.persistence.repository.SpringDataOrderRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class OrderPersistenceAdapter implements OrderRepositoryPort {

    private final SpringDataOrderRepository springDataOrderRepository;

    public OrderPersistenceAdapter(SpringDataOrderRepository springDataOrderRepository) {
        this.springDataOrderRepository = springDataOrderRepository;
    }

    @Override
    public Order save(Order order) {
        OrderJpaEntity entity = toJpaEntity(order);
        OrderJpaEntity saved = springDataOrderRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return springDataOrderRepository.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsById(UUID id) {
        return springDataOrderRepository.existsById(id);
    }

    private OrderJpaEntity toJpaEntity(Order order) {
        OrderJpaEntity entity = new OrderJpaEntity(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );

        for (OrderItem item : order.getItems()) {
            OrderItemJpaEntity itemEntity = new OrderItemJpaEntity(
                    item.getId(),
                    entity,
                    item.getProductId(),
                    item.getQuantity(),
                    item.getUnitPrice()
            );
            entity.addItem(itemEntity);
        }

        return entity;
    }

    private Order toDomain(OrderJpaEntity entity) {
        List<OrderItem> domainItems = entity.getItems().stream()
                .map(item -> new OrderItem(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice()
                ))
                .toList();

        return new Order(
                entity.getId(),
                entity.getCustomerId(),
                entity.getStatus(),
                domainItems,
                entity.getTotalAmount(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
