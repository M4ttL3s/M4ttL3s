package com.distributed.inventory.domain.model;

import com.distributed.inventory.domain.exception.InventoryDomainException;

import java.util.UUID;

public class Product {

    private final UUID id;
    private int stock;

    public Product(UUID id, int stock) {
        if (id == null) {
            throw new InventoryDomainException("Product ID cannot be null");
        }
        if (stock < 0) {
            throw new InventoryDomainException("Initial stock cannot be negative");
        }
        this.id = id;
        this.stock = stock;
    }

    public static Product create(UUID id, int stock) {
        return new Product(id != null ? id : UUID.randomUUID(), stock);
    }

    public UUID getId() {
        return id;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        if (stock < 0) {
            throw new InventoryDomainException("Stock cannot be negative");
        }
        this.stock = stock;
    }
}
