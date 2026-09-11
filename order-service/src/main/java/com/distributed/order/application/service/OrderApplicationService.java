package com.distributed.order.application.service;

import com.distributed.order.application.dto.CreateOrderCommand;
import com.distributed.order.application.dto.OrderItemResponse;
import com.distributed.order.application.dto.OrderResponse;
import com.distributed.order.application.ports.in.CancelOrderUseCase;
import com.distributed.order.application.ports.in.ConfirmOrderUseCase;
import com.distributed.order.application.ports.in.CreateOrderUseCase;
import com.distributed.order.application.ports.in.GetOrderUseCase;
import com.distributed.order.application.ports.in.RejectOrderUseCase;
import com.distributed.order.domain.exception.DuplicateIdempotencyKeyException;
import com.distributed.order.domain.exception.OrderNotFoundException;
import com.distributed.order.domain.model.IdempotencyRecord;
import com.distributed.order.domain.model.Order;
import com.distributed.order.domain.model.OrderItem;
import com.distributed.order.domain.model.OrderStatus;
import com.distributed.order.domain.repository.IdempotencyKeyRepositoryPort;
import com.distributed.order.domain.repository.OrderEventPublisherPort;
import com.distributed.order.domain.repository.OrderRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OrderApplicationService implements CreateOrderUseCase, GetOrderUseCase,
        CancelOrderUseCase, ConfirmOrderUseCase, RejectOrderUseCase {

    private static final Logger log = LoggerFactory.getLogger(OrderApplicationService.class);

    private final OrderRepositoryPort orderRepositoryPort;
    private final IdempotencyKeyRepositoryPort idempotencyKeyRepositoryPort;
    private final OrderEventPublisherPort orderEventPublisherPort;

    public OrderApplicationService(OrderRepositoryPort orderRepositoryPort,
                                   IdempotencyKeyRepositoryPort idempotencyKeyRepositoryPort,
                                   OrderEventPublisherPort orderEventPublisherPort) {
        this.orderRepositoryPort = orderRepositoryPort;
        this.idempotencyKeyRepositoryPort = idempotencyKeyRepositoryPort;
        this.orderEventPublisherPort = orderEventPublisherPort;
    }

    @Override
    public OrderResponse createOrder(CreateOrderCommand command) {
        String key = command.idempotencyKey();

        if (key != null && !key.isBlank()) {
            Optional<IdempotencyRecord> record = idempotencyKeyRepositoryPort.findByKey(key);
            if (record.isPresent()) {
                log.info("Idempotency key {} already processed. Returning existing order.", key);
                UUID orderId = record.get().getOrderId();
                return orderRepositoryPort.findById(orderId)
                        .map(this::mapToResponse)
                        .orElseThrow(() -> new DuplicateIdempotencyKeyException(key));
            }
        }

        log.info("Processing new order creation for customer: {}", command.customerId());

        List<OrderItem> items = command.items().stream()
                .map(i -> new OrderItem(null, i.productId(), i.quantity(), i.unitPrice()))
                .toList();

        Order order = Order.create(command.customerId(), items);
        Order saved = orderRepositoryPort.save(order);

        if (key != null && !key.isBlank()) {
            idempotencyKeyRepositoryPort.save(new IdempotencyRecord(
                    key,
                    saved.getId(),
                    201,
                    "Order created successfully",
                    LocalDateTime.now()
            ));
        }

        orderEventPublisherPort.publishOrderCreated(saved);
        log.info("Order {} created and event published.", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        return orderRepositoryPort.findById(orderId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Override
    public OrderResponse cancelOrder(UUID orderId) {
        log.info("Attempting to cancel order: {}", orderId);
        Order order = orderRepositoryPort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        order.cancel();
        Order saved = orderRepositoryPort.save(order);

        orderEventPublisherPort.publishOrderCancelled(orderId);
        log.info("Order {} cancelled and event published.", orderId);

        return mapToResponse(saved);
    }

    @Override
    public void confirmOrder(UUID orderId) {
        orderRepositoryPort.findById(orderId).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.PENDING) {
                log.info("Confirming order: {}", orderId);
                order.confirm();
                orderRepositoryPort.save(order);
            }
        });
    }

    @Override
    public void rejectOrder(UUID orderId, String reason) {
        orderRepositoryPort.findById(orderId).ifPresent(order -> {
            if (order.getStatus() == OrderStatus.PENDING) {
                log.warn("Rejecting order {}. Reason: {}", orderId, reason);
                order.reject();
                orderRepositoryPort.save(order);
            }
        });
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getId(),
                        item.getProductId(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
