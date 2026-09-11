package com.distributed.order.domain.exception;

public class DuplicateIdempotencyKeyException extends RuntimeException {

    private final String idempotencyKey;

    public DuplicateIdempotencyKeyException(String idempotencyKey) {
        super("A request with Idempotency-Key '" + idempotencyKey + "' is already being processed or has completed.");
        this.idempotencyKey = idempotencyKey;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }
}
