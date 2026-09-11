package com.distributed.order.domain.model;

import com.distributed.order.domain.exception.OrderDomainException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Order {

    private final UUID id;
    private final String customerId;
    private OrderStatus status;
    private final List<OrderItem> items;
    private BigDecimal totalAmount;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order(UUID id, String customerId, OrderStatus status, List<OrderItem> items,
                 BigDecimal totalAmount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id != null ? id : UUID.randomUUID();
        this.customerId = customerId;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.items = items != null ? new ArrayList<>(items) : new ArrayList<>();
        this.totalAmount = totalAmount != null ? totalAmount : calculateTotalAmount();
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public static Order create(String customerId, List<OrderItem> items) {
        if (customerId == null || customerId.isBlank()) {
            throw new OrderDomainException("Customer ID is required");
        }
        if (items == null || items.isEmpty()) {
            throw new OrderDomainException("An order must contain at least one item");
        }

        UUID orderId = UUID.randomUUID();
        BigDecimal total = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Order(orderId, customerId, OrderStatus.PENDING, items, total, LocalDateTime.now(), LocalDateTime.now());
    }

    public void cancel() {
        if (this.status == OrderStatus.CANCELLED) {
            throw new OrderDomainException("Order " + id + " is already cancelled");
        }
        if (this.status == OrderStatus.REJECTED) {
            throw new OrderDomainException("Cannot cancel an already rejected order: " + id);
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        if (this.status != OrderStatus.PENDING) {
            throw new OrderDomainException("Cannot confirm order in status: " + this.status);
        }
        this.status = OrderStatus.CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    public void reject() {
        if (this.status != OrderStatus.PENDING) {
            throw new OrderDomainException("Cannot reject order in status: " + this.status);
        }
        this.status = OrderStatus.REJECTED;
        this.updatedAt = LocalDateTime.now();
    }

    private BigDecimal calculateTotalAmount() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public UUID getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
