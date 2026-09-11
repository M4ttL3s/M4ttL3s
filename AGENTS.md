# Project AI Assistant Conventions

Este documento establece las reglas arquitectónicas y convenciones de código para cualquier agente de IA o asistente (Copilot, Antigravity, etc.) utilizado como soporte en el desarrollo de este repositorio.

## 1. Restricciones de Arquitectura
- **Aislamiento Estricto:** `order-service` e `inventory-service` son dominios separados. Tienen prohibido compartir código, entidades o esquemas de base de datos.
- **Arquitectura Hexagonal:** Respeta siempre la separación entre `domain` (lógica agnóstica), `application` (casos de uso) e `infrastructure` (adaptadores REST/JPA/AMQP).

## 2. Concurrencia y Persistencia
- **Zero JVM Locks:** Está estrictamente prohibido el uso de `synchronized`. 
- **Bloqueos Distribuidos:** Cualquier modificación de inventario debe delegarse al motor transaccional (PostgreSQL) mediante sentencias atómicas.

## 3. Mensajería Asincrónica
- **Idempotencia First:** Asume siempre que la red es falible (at-least-once). Valida la clave de idempotencia antes de mutar estado.
- **Race Conditions:** Utiliza el estado centinela `CANCEL_PENDING` para garantizar consistencia eventual ante mensajes desordenados.
