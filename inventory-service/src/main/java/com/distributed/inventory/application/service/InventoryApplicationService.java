package com.distributed.inventory.application.service;

import com.distributed.inventory.application.dto.CreateProductCommand;
import com.distributed.inventory.application.dto.ProductStockResponse;
import com.distributed.inventory.application.dto.ReservationResponse;
import com.distributed.inventory.application.dto.ReserveStockCommand;
import com.distributed.inventory.application.ports.in.CancelReservationUseCase;
import com.distributed.inventory.application.ports.in.CreateProductUseCase;
import com.distributed.inventory.application.ports.in.GetProductStockUseCase;
import com.distributed.inventory.application.ports.in.ReserveStockUseCase;
import com.distributed.inventory.domain.exception.ProductNotFoundException;
import com.distributed.inventory.domain.model.Product;
import com.distributed.inventory.domain.model.Reservation;
import com.distributed.inventory.domain.model.ReservationStatus;
import com.distributed.inventory.domain.repository.ProductRepositoryPort;
import com.distributed.inventory.domain.repository.ReservationRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class InventoryApplicationService implements CreateProductUseCase,
        GetProductStockUseCase, ReserveStockUseCase, CancelReservationUseCase {

    private static final Logger log = LoggerFactory.getLogger(InventoryApplicationService.class);

    private final ProductRepositoryPort productRepositoryPort;
    private final ReservationRepositoryPort reservationRepositoryPort;

    public InventoryApplicationService(ProductRepositoryPort productRepositoryPort,
                                       ReservationRepositoryPort reservationRepositoryPort) {
        this.productRepositoryPort = productRepositoryPort;
        this.reservationRepositoryPort = reservationRepositoryPort;
    }

    @Override
    public ProductStockResponse createProduct(CreateProductCommand command) {
        UUID productId = command.id() != null ? command.id() : UUID.randomUUID();
        log.info("Creating product with ID: {} and initial stock: {}", productId, command.initialStock());
        Product product = Product.create(productId, command.initialStock());
        Product saved = productRepositoryPort.save(product);
        return new ProductStockResponse(saved.getId(), saved.getStock());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductStockResponse getProductStock(UUID productId) {
        Product product = productRepositoryPort.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        return new ProductStockResponse(product.getId(), product.getStock());
    }

    @Override
    public ReservationResponse reserveStock(ReserveStockCommand command) {
        UUID orderId = command.orderId();
        UUID productId = command.productId();
        int quantity = command.quantity();

        log.info("Processing stock reservation for Order ID: {}, Product ID: {}, Quantity: {}", orderId, productId, quantity);

        Optional<Reservation> existingReservation = reservationRepositoryPort.findByOrderId(orderId);
        if (existingReservation.isPresent()) {
            Reservation existing = existingReservation.get();

            // CASO CONDICIÓN DE CARRERA: OrderCancelled llegó ANTES que OrderCreated
            if (existing.getStatus() == ReservationStatus.CANCEL_PENDING) {
                log.info("Race condition detected: CANCEL_PENDING found for Order ID: {}. Marking as CANCELLED.", orderId);
                existing.markCancelledFromPending(productId, quantity);
                Reservation updated = reservationRepositoryPort.save(existing);
                return mapToResponse(updated);
            }

            // CASO IDEMPOTENCIA: El evento ya fue procesado ("at-least-once")
            log.info("Idempotency hit: Reservation already exists for Order ID: {}", orderId);
            return mapToResponse(existing);
        }

        if (!productRepositoryPort.existsById(productId)) {
            log.warn("Reservation failed. Product ID: {} not found.", productId);
            throw new ProductNotFoundException(productId);
        }

        // UPDATE products SET stock = stock - :qty WHERE id = :id AND stock >= :qty
        int rowsUpdated = productRepositoryPort.deductStockAtomically(productId, quantity);

        Reservation reservation;
        if (rowsUpdated == 1) {
            log.info("Stock deducted successfully for Order ID: {}", orderId);
            reservation = Reservation.createReserved(orderId, productId, quantity);
        } else {
            log.warn("Insufficient stock for Product ID: {}. Rejecting reservation for Order ID: {}", productId, orderId);
            reservation = Reservation.createRejected(orderId, productId, quantity);
        }

        Reservation saved = reservationRepositoryPort.save(reservation);
        return mapToResponse(saved);
    }

    @Override
    public ReservationResponse cancelReservation(UUID orderId) {
        log.info("Attempting to cancel reservation for Order ID: {}", orderId);
        Optional<Reservation> existingReservation = reservationRepositoryPort.findByOrderId(orderId);

        if (existingReservation.isEmpty()) {
            // CASO CONDICIÓN DE CARRERA (Race Condition):
            // OrderCancelled llega antes de que OrderCreated haya sido procesado.
            // Se crea un registro centinela en estado CANCEL_PENDING.
            log.info("Reservation not found for Order ID: {}. Creating CANCEL_PENDING sentinel.", orderId);
            Reservation sentinel = Reservation.createCancelPending(orderId);
            Reservation savedSentinel = reservationRepositoryPort.save(sentinel);
            return mapToResponse(savedSentinel);
        }

        Reservation existing = existingReservation.get();

        if (existing.getStatus() == ReservationStatus.RESERVED) {
            log.info("Restoring stock for cancelled Order ID: {}", orderId);
            productRepositoryPort.restoreStockAtomically(existing.getProductId(), existing.getQuantity());
            existing.cancel();
            Reservation updated = reservationRepositoryPort.save(existing);
            return mapToResponse(updated);
        }

        log.info("Reservation for Order ID: {} is already cancelled or rejected. No action taken.", orderId);
        return mapToResponse(existing);
    }

    private ReservationResponse mapToResponse(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getOrderId(),
                reservation.getProductId(),
                reservation.getQuantity(),
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getUpdatedAt()
        );
    }
}
