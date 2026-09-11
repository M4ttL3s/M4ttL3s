package com.distributed.order.infrastructure.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank(message = "Customer ID is mandatory")
        String customerId,

        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<CreateOrderItemRequest> items
) {}
