package com.distributed.order.application.ports.in;

import java.util.UUID;

public interface RejectOrderUseCase {
    void rejectOrder(UUID orderId, String reason);
}
