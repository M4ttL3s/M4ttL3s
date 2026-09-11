package com.distributed.inventory.domain.repository;

import com.distributed.inventory.domain.model.Product;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepositoryPort {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    boolean existsById(UUID id);

    /**
     * Actualización atómica en base de datos.
     * Retorna 1 si el stock fue descontado exitosamente, 0 si el stock era insuficiente.
     */
    int deductStockAtomically(UUID productId, int quantity);

    /**
     * Incremento atómico en base de datos al cancelar una reserva.
     */
    int restoreStockAtomically(UUID productId, int quantity);
}
