package com.distributed.inventory.application.ports.in;

import com.distributed.inventory.application.dto.ReservationResponse;
import com.distributed.inventory.application.dto.ReserveStockCommand;

public interface ReserveStockUseCase {
    ReservationResponse reserveStock(ReserveStockCommand command);
}
