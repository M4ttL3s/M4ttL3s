package com.distributed.order.domain.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class IdempotencyRecord {

    private final String key;
    private final UUID orderId;
    private final int statusCode;
    private final String responsePayload;
    private final LocalDateTime createdAt;

    public IdempotencyRecord(String key, UUID orderId, int statusCode, String responsePayload, LocalDateTime createdAt) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Idempotency key cannot be blank");
        }
        this.key = key;
        this.orderId = orderId;
        this.statusCode = statusCode;
        this.responsePayload = responsePayload;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public String getKey() {
        return key;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getResponsePayload() {
        return responsePayload;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
