package com.distributed.order.infrastructure.rest.controller;

import com.distributed.order.application.dto.CreateOrderCommand;
import com.distributed.order.application.dto.OrderItemCommand;
import com.distributed.order.application.dto.OrderResponse;
import com.distributed.order.application.ports.in.CancelOrderUseCase;
import com.distributed.order.application.ports.in.CreateOrderUseCase;
import com.distributed.order.application.ports.in.GetOrderUseCase;
import com.distributed.order.infrastructure.rest.dto.CreateOrderRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final GetOrderUseCase getOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    public OrderController(CreateOrderUseCase createOrderUseCase,
                           GetOrderUseCase getOrderUseCase,
                           CancelOrderUseCase cancelOrderUseCase) {
        this.createOrderUseCase = createOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody CreateOrderRequest request) {

        List<OrderItemCommand> itemCommands = request.items().stream()
                .map(item -> new OrderItemCommand(item.productId(), item.quantity(), item.unitPrice()))
                .toList();

        CreateOrderCommand command = new CreateOrderCommand(
                request.customerId(),
                itemCommands,
                idempotencyKey
        );

        OrderResponse response = createOrderUseCase.createOrder(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable UUID orderId) {
        OrderResponse response = getOrderUseCase.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable UUID orderId) {
        OrderResponse response = cancelOrderUseCase.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }
}
