# Sistema Distribuido (Saga)

Este proyecto implementa el patrón Saga Coreografiado utilizando Spring Boot 3, Java 25, PostgreSQL y RabbitMQ. Está compuesto por dos microservicios:
1. `order-service` (Servicio de Órdenes)
2. `inventory-service` (Servicio de Inventario)

## Características Principales

- **Arquitectura Hexagonal:** Separación estricta entre las capas de Dominio, Aplicación e Infraestructura.
- **Aislamiento de Bases de Datos:** Cada servicio cuenta con su propia base de datos lógica en PostgreSQL (`order_db` e `inventory_db`), previniendo cruces de esquemas o tablas compartidas.
- **Concurrencia Distribuida:** Las actualizaciones de inventario se gestionan atómicamente mediante sentencias `UPDATE` en PostgreSQL, evitando completamente el uso de bloqueos a nivel de JVM (`synchronized`) que fallan en entornos distribuidos.
- **Idempotencia:** Tanto las APIs REST como los endpoints consumidores de mensajes implementan mecanismos de idempotencia para asegurar semántica de entrega `at-least-once` (al menos una vez).
- **Manejo de Condiciones de Carrera:** Se utilizan estados centinela (`CANCEL_PENDING`) para gestionar de forma asíncrona aquellos mensajes que llegan en desorden (ej: cuando un evento de cancelación llega antes que el evento de creación).
- **Colas de Mensajes Muertos (DLQ):** RabbitMQ está configurado con Dead Letter Queues para enrutar mensajes "envenenados" o con fallos sin bloquear los hilos de los consumidores principales.

## Requisitos Previos

- Java 25+
- Docker & Docker Compose
- Maven 3.9+

## Iniciar la Infraestructura

Para arrancar los servicios de infraestructura (PostgreSQL y RabbitMQ):

```bash
docker-compose up -d
```

## Ejecutar los Microservicios

Arranca los servicios utilizando el Maven Wrapper (`mvnw`) dentro de cada directorio:

### Servicio de Órdenes (Puerto 8080)
```bash
cd order-service
./mvnw spring-boot:run
```

### Servicio de Inventario (Puerto 8081)
```bash
cd inventory-service
./mvnw spring-boot:run
```

## Pruebas (Tests)

El proyecto incluye pruebas unitarias (utilizando Mockito) y pruebas de integración para concurrencia masiva (con hilos paralelos sobre base de datos). Para ejecutarlas:

```bash
cd order-service
./mvnw test

cd ../inventory-service
./mvnw test
```

## Pruebas de la API REST

Se incluye una colección en el directorio `bruno/` lista para importar en el cliente **Bruno** y probar los siguientes casos de uso:
- Crear Producto Inicial (Inventario)
- Crear una Orden (Order)
- Cancelar una Orden (Order)
- Consultar el estado de una Orden (Order)
- Consultar el Stock disponible de un Producto (Inventario)
