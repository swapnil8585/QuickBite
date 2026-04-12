# QuickBite — Real-Time Food Delivery Platform

A production-grade microservices backend built with **Spring Boot**, **Apache Kafka**, **Redis**, and **MySQL**.

## Architecture

| Service              | Port | Responsibilities                                     |
|----------------------|------|------------------------------------------------------|
| API Gateway          | 8080 | JWT auth, routing, rate limiting                     |
| User Service         | 8081 | Registration, login, profiles                        |
| Order Service        | 8082 | Place & track orders, Kafka producer                 |
| Restaurant Service   | 8083 | Menus, availability, Redis menu cache                |
| Delivery Service     | 8084 | Driver tracking, Redis location store                |
| Payment Service      | 8085 | Payment processing, Kafka consumer                   |
| Notification Service | 8086 | Email/push via Kafka consumers                       |

## Kafka Topics

| Topic            | Producer        | Consumers                           |
|------------------|-----------------|-------------------------------------|
| order-placed     | Order Service   | Payment Service, Notification Service |
| order-updated    | Order Service   | Delivery Service, Notification Service |
| order-cancelled  | Order Service   | Notification Service                |
| payment-success  | Payment Service | Order Service, Notification Service |

## Redis Usage

| Key Pattern                   | Data                     | TTL      |
|-------------------------------|--------------------------|----------|
| `driver:{id}:location`        | lat,lng string           | 5 min    |
| `orders::{orderId}`           | Order JSON (Spring Cache) | evicted on update |
| `restaurants::all-open`       | Restaurant list           | evicted on toggle |
| `menus::{restaurantId}`       | Menu item list            | evicted on change |

## Quick Start

### 1. Start infrastructure
```bash
docker-compose up -d
```

### 2. Run services (in separate terminals)
```bash
cd user-service        && mvn spring-boot:run
cd order-service       && mvn spring-boot:run
cd restaurant-service  && mvn spring-boot:run
cd delivery-service    && mvn spring-boot:run
cd payment-service     && mvn spring-boot:run
cd notification-service && mvn spring-boot:run
cd api-gateway         && mvn spring-boot:run
```

### 3. Test the happy path

```bash
# Register
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123","fullName":"John Doe"}'

# Login — copy the token from response
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'

# Place order (replace <TOKEN>)
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": 1,
    "deliveryAddress": "123 Main St",
    "items": [
      {"menuItemId": 1, "itemName": "Burger", "quantity": 2, "unitPrice": 150.00}
    ]
  }'
```

### 4. Monitor Kafka
Open **http://localhost:8090** to see Kafka topics and messages in real time.

## Tech Stack

- **Java 17** + **Spring Boot 3.2**
- **Spring Cloud Gateway** (API Gateway)
- **Spring Security** + **JWT** (Authentication)
- **Apache Kafka** (Event streaming)
- **Redis** (Caching + real-time location)
- **MySQL 8** (Persistent storage — database per service)
- **MapStruct** (DTO mapping)
- **Lombok** (Boilerplate reduction)
- **Docker Compose** (Local infrastructure)

## Resume Talking Points

- Implemented **database-per-service** pattern ensuring loose coupling between microservices
- Used **Kafka** for asynchronous event-driven communication — placing an order triggers payment and notification flows without direct coupling
- Applied **Redis caching** at two levels: menu data (read-heavy) and driver GPS location (write-heavy, sub-second TTL)
- Built a **JWT-based auth filter** in the API Gateway so downstream services trust forwarded headers
- Designed for **idempotency** in the Payment Service to prevent duplicate charges
