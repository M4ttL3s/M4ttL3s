package com.distributed.inventory.domain.model;

import com.distributed.inventory.domain.exception.InventoryDomainException;

import java.time.LocalDateTime;
import java.util.UUID;

public class Reservation {

    private final UUID id;
    private final UUID orderId;
    private UUID productId;
    private int quantity;
    private ReservationStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Reservation(UUID id, UUID orderId, UUID productId, int quantity,
                       ReservationStatus status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        if (orderId == null) {
            throw new InventoryDomainException("Order ID cannot be null");
        }
        this.id = id != null ? id : UUID.randomUUID();
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.status = status != null ? status : ReservationStatus.RESERVED;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.updatedAt = updatedAt != null ? updatedAt : LocalDateTime.now();
    }

    public static Reservation createReserved(UUID orderId, UUID productId, int quantity) {
        return new Reservation(UUID.randomUUID(), orderId, productId, quantity,
                ReservationStatus.RESERVED, LocalDateTime.now(), LocalDateTime.now());
    }

    public static Reservation createRejected(UUID orderId, UUID productId, int quantity) {
        return new Reservation(UUID.randomUUID(), orderId, productId, quantity,
                ReservationStatus.REJECTED, LocalDateTime.now(), LocalDateTime.now());
    }

    public static Reservation createCancelPending(UUID orderId) {
        return new Reservation(UUID.randomUUID(), orderId, null, 0,
                ReservationStatus.CANCEL_PENDING, LocalDateTime.now(), LocalDateTime.now());
    }

    public void cancel() {
        if (this.status == ReservationStatus.CANCELLED) {
            return; // Idempotente
        }
        this.status = ReservationStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void markCancelledFromPending(UUID productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
        this.status = ReservationStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
