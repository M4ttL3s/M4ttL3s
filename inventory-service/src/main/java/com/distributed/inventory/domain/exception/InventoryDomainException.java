package com.distributed.inventory.domain.exception;

public class InventoryDomainException extends RuntimeException {
    public InventoryDomainException(String message) {
        super(message);
    }
}
