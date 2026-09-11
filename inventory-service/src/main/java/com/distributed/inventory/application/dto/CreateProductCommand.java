package com.distributed.inventory.application.dto;

import java.util.UUID;

public record CreateProductCommand(
        UUID id,
        int initialStock
) {}
