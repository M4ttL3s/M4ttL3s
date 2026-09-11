package com.distributed.inventory.domain.repository;

import java.util.UUID;

public interface InventoryEventPublisherPort {

    void publishStockReserved(UUID orderId, UUID productId, int quantity);

    void publishStockRejected(UUID orderId, UUID productId, int quantity, String reason);
}
