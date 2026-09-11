package com.distributed.order.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "idempotency_keys",
    indexes = {
        @Index(name = "idx_idempotency_key_unique", columnList = "idempotency_key", unique = true)
    }
)
public class IdempotencyKeyJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "status_code", nullable = false)
    private int statusCode;

    @Column(name = "response_payload", length = 4096)
    private String responsePayload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public IdempotencyKeyJpaEntity() {
    }

    public IdempotencyKeyJpaEntity(String idempotencyKey, UUID orderId, int statusCode,
                                  String responsePayload, LocalDateTime createdAt) {
        this.idempotencyKey = idempotencyKey;
        this.orderId = orderId;
        this.statusCode = statusCode;
        this.responsePayload = responsePayload;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public void setResponsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
