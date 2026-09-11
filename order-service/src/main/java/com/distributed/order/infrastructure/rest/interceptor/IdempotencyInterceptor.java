package com.distributed.order.infrastructure.rest.interceptor;

import com.distributed.order.application.dto.OrderResponse;
import com.distributed.order.application.ports.in.GetOrderUseCase;
import com.distributed.order.domain.model.IdempotencyRecord;
import com.distributed.order.domain.repository.IdempotencyKeyRepositoryPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    public static final String IDEMPOTENCY_HEADER = "Idempotency-Key";

    private final IdempotencyKeyRepositoryPort idempotencyKeyRepositoryPort;
    private final GetOrderUseCase getOrderUseCase;
    private final ObjectMapper objectMapper;

    public IdempotencyInterceptor(IdempotencyKeyRepositoryPort idempotencyKeyRepositoryPort,
                                  GetOrderUseCase getOrderUseCase,
                                  ObjectMapper objectMapper) {
        this.idempotencyKeyRepositoryPort = idempotencyKeyRepositoryPort;
        this.getOrderUseCase = getOrderUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Solo interceptar POST /orders
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return true;
        }

        String path = request.getRequestURI();
        if (!path.endsWith("/orders") && !path.endsWith("/orders/")) {
            return true;
        }

        String idempotencyKey = request.getHeader(IDEMPOTENCY_HEADER);
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            // Si el cliente no provee la cabecera, se permite continuar normalmente
            return true;
        }

        // Validación en PostgreSQL: si la clave ya existe, responder el recurso existente (Idempotente)
        Optional<IdempotencyRecord> recordOpt = idempotencyKeyRepositoryPort.findByKey(idempotencyKey);
        if (recordOpt.isPresent()) {
            IdempotencyRecord record = recordOpt.get();
            OrderResponse existingOrder = getOrderUseCase.getOrderById(record.getOrderId());

            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("X-Idempotent-Replay", "true");
            response.getWriter().write(objectMapper.writeValueAsString(existingOrder));
            response.getWriter().flush();

            // Detener la cadena de ejecución: no se vuelve a crear la orden
            return false;
        }

        return true;
    }
}
