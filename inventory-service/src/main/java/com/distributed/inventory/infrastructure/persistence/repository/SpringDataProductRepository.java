package com.distributed.inventory.infrastructure.persistence.repository;

import com.distributed.inventory.infrastructure.persistence.entity.ProductJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, UUID> {

    /**
     * REGLA ESTRICTA DE CONCURRENCIA DISTRIBUIDA:
     * Actualización atómica en el motor de base de datos PostgreSQL.
     * Garantiza aislamiento ACID sin utilizar synchronized ni bloqueos en memoria de la JVM.
     * Retorna 1 si el stock fue decrementado, o 0 si no cumplió la condición de stock suficiente.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE products SET stock = stock - :qty WHERE id = :productId AND stock >= :qty", nativeQuery = true)
    int deductStockAtomically(@Param("productId") UUID productId, @Param("qty") int qty);

    /**
     * Incremento atómico en PostgreSQL para liberar stock ante una cancelación.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE products SET stock = stock + :qty WHERE id = :productId", nativeQuery = true)
    int restoreStockAtomically(@Param("productId") UUID productId, @Param("qty") int qty);
}
