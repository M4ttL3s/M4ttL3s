package com.distributed.order.application.ports.in;

import java.util.UUID;

public interface ConfirmOrderUseCase {
    void confirmOrder(UUID orderId);
}
