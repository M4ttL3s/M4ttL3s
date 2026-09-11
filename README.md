# Sistema Distribuido - Saga Coreografiado

Este proyecto es una prueba tecnica orientada a roles Backend Senior. Implementa el patron Saga Coreografiado utilizando Spring Boot 3 (Java 25), PostgreSQL y RabbitMQ.

El sistema esta compuesto por dos microservicios independientes:
1. `order-service` (Servicio de Ordenes - Puerto 8081)
2. `inventory-service` (Servicio de Inventario - Puerto 8082)

## Decisiones Arquitectonicas y Tecnicas

- **Arquitectura Hexagonal:** Separacion estricta entre las capas de Dominio, Aplicacion e Infraestructura. El dominio no depende de Spring ni de ningun framework.
- **Aislamiento de Bases de Datos:** Cada servicio cuenta con su propia base de datos logica en PostgreSQL (`order_db` e `inventory_db`). No comparten tablas ni esquemas.
- **Concurrencia Distribuida (Zero JVM Locks):** Las actualizaciones de inventario se gestionan atomicamente mediante sentencias `UPDATE` condicionales en PostgreSQL (`UPDATE products SET stock = stock - :qty WHERE id = :id AND stock >= :qty`). No se utiliza `synchronized` ni bloqueos en memoria de la JVM.
- **Idempotencia (At-least-once):** Las APIs REST validan el header `Idempotency-Key` contra una tabla local antes de mutar estado. Los listeners de RabbitMQ verifican la existencia previa del `eventId` en la tabla `processed_events`.
- **Condiciones de Carrera:** Se utilizan estados centinela (`CANCEL_PENDING`) para gestionar asincronia desordenada. Si un evento de cancelacion llega antes que el de creacion, el sistema crea un registro centinela y al llegar la creacion detecta el conflicto y marca la reserva como cancelada.
- **Dead Letter Queues (DLQ):** RabbitMQ esta configurado con colas muertas (`x-dead-letter-exchange`) para enrutar mensajes fallidos sin bloquear los consumidores principales.
- **Observabilidad y Trazabilidad (Bonus):** Integracion con Micrometer Tracing (Brave). Los logs incluyen `traceId` y `spanId` via el patron `%X{traceId}`. Endpoints de salud y metricas expuestos via Spring Boot Actuator.

## Sistema de Mensajeria

Para la comunicacion asincronica entre microservicios se ha elegido **RabbitMQ**.

### Motivo de la eleccion
RabbitMQ ofrece enrutamiento flexible mediante Exchanges y Routing Keys, ideal para el patron Saga Coreografiado donde cada servicio publica y consume eventos de dominio de forma independiente. Es ligero, tiene excelente documentacion, una interfaz de gestion web integrada y soporte nativo para Dead Letter Queues. Para el volumen y la complejidad de esta prueba, RabbitMQ es mas pragmatico que Kafka, que esta orientado a streaming de alto volumen y requiere gestion de offsets y particiones.

### Garantia de entrega utilizada
Se utiliza semantica **At-least-once**. Se asume que la red es falible y que un mismo evento puede llegar dos o mas veces. Para mitigar esto, cada consumidor valida el `eventId` del mensaje contra una tabla local (`processed_events`) antes de ejecutar la logica de negocio. Si el evento ya fue procesado, se descarta silenciosamente.

### Estrategia de reintentos
Se configuran reintentos locales automaticos con **retroceso exponencial** (exponential backoff) a nivel de Spring AMQP:
- Intervalo inicial: 1 segundo
- Maximo de intentos: 3
- Multiplicador: 2.0

Esto permite absorber errores transitorios (timeout de red, indisponibilidad temporal de la base de datos) sin saturar el broker.

### Manejo de mensajes que no pueden procesarse
Si un mensaje agota los reintentos o produce un error fatal (deserializacion, validacion de negocio irrecuperable), se rechaza sin reencolar (`default-requeue-rejected: false`). RabbitMQ lo enruta automaticamente mediante el argumento `x-dead-letter-exchange` hacia una cola de mensajes muertos (DLQ) dedicada por servicio:
- `inventory.order-created.dlq`
- `inventory.order-cancelled.dlq`
- `order.stock-response.dlq`

Estos mensajes quedan disponibles para inspeccion y reprocesamiento manual.

### Orden de procesamiento
El sistema asume **asincronia desordenada**. No se garantiza el orden de llegada de los eventos entre servicios. Para manejar esto, se utilizan estados centinela (`CANCEL_PENDING`) en la base de datos del inventario. Si una cancelacion llega antes que la creacion de la reserva, se persiste un registro centinela. Cuando posteriormente llega el evento de creacion, el sistema detecta el centinela y resuelve el conflicto garantizando consistencia eventual.

## Requisitos e Infraestructura

- Java 25+
- Docker y Docker Compose
- Maven 3.9+

En la raiz encontraras el archivo `docker-compose.yml`. Este archivo aprovisiona toda la infraestructura necesaria: PostgreSQL 16 (con un script de inicializacion que crea `order_db` e `inventory_db` de forma aislada) y RabbitMQ 3 con interfaz de gestion.

Para iniciar la infraestructura:
```bash
docker-compose up -d
```

## Ejecucion

Utiliza el Wrapper de Maven en cada directorio:
```bash
# Order Service (Puerto 8081)
cd order-service
.\mvnw spring-boot:run

# Inventory Service (Puerto 8082)
cd inventory-service
.\mvnw spring-boot:run
```

## Pruebas

El proyecto incluye tests unitarios (Mockito) y una prueba de integracion de concurrencia que lanza 100 hilos simultaneos contra un producto con stock 10, verificando que exactamente 10 reservas sean exitosas y el stock final sea 0.

```bash
cd order-service
.\mvnw test

cd inventory-service
.\mvnw test
```

## Coleccion de APIs (Bruno)

En la carpeta `bruno/` encontraras la coleccion exportada, lista para importar en el cliente Bruno y probar todo el flujo asincrono:
1. Crear Producto (POST /products)
2. Crear Orden (POST /orders)
3. Consultar Orden (GET /orders/{orderId})
4. Cancelar Orden (POST /orders/{orderId}/cancel)
5. Consultar Stock (GET /products/{productId}/stock)

## Endpoints

### Order Service (Puerto 8081)
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| POST | /orders | Crear pedido |
| GET | /orders/{orderId} | Consultar estado de pedido |
| POST | /orders/{orderId}/cancel | Cancelar pedido |

### Inventory Service (Puerto 8082)
| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| POST | /products | Cargar stock de producto |
| GET | /products/{productId}/stock | Consultar stock disponible |
