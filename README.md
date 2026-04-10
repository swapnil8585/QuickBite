# QuickBite 🍔 — Real-Time Food Delivery Platform

A production-grade, full-stack food delivery platform built with **Java 17**, **Spring Boot 3.2**, **Angular 17**, **Apache Kafka**, **Redis**, and **MySQL** — deployed on **AWS**. Designed using microservices architecture with event-driven communication, real-time GPS tracking, and JWT-based security.

> Built as a personal project to demonstrate distributed systems design, event-driven architecture, and full-stack development skills.

---

## 🏗️ Architecture Overview

```
                        ┌─────────────────────┐
                        │   Angular 17 Frontend│
                        │  (3 Portals)         │
                        └─────────┬───────────┘
                                  │ REST / WebSocket
                        ┌─────────▼───────────┐
                        │    API Gateway       │
                        │  Spring Cloud Gateway│
                        │  JWT Auth · Routing  │
                        │  Redis Rate Limiting │
                        └──┬──┬──┬──┬──┬──┬──┘
                           │  │  │  │  │  │
          ┌────────────────┘  │  │  │  │  └──────────────────┐
          │          ┌────────┘  │  │  └────────┐            │
          ▼          ▼           ▼  ▼           ▼            ▼
    ┌──────────┐ ┌──────────┐ ┌──────┐ ┌──────────┐ ┌────────────┐
    │  User    │ │  Order   │ │ Rest.│ │ Delivery │ │  Payment   │
    │ Service  │ │ Service  │ │ Svc  │ │ Service  │ │  Service   │
    │  :8081   │ │  :8082   │ │ :8083│ │  :8084   │ │   :8085    │
    └──────────┘ └────┬─────┘ └──────┘ └──────────┘ └────────────┘
                      │                                      ▲
                      │         Apache Kafka                 │
                      └──── order-placed ────────────────────┘
                           order-updated ──► Delivery Svc
                           payment-success ─► Notification Svc
                                         ┌────────────┐
                                         │Notification│
                                         │  Service   │
                                         │   :8086    │
                                         └────────────┘
```

---

## ✨ Features

### Customer Portal
- Browse restaurants by cuisine, location, and rating
- View restaurant menus with live availability
- Place orders with multiple items and special instructions
- Real-time order status tracking (7 stages)
- Live driver GPS tracking on map
- Order history and payment records

### Restaurant Owner Portal
- Manage restaurant profile and operating hours (open/close toggle)
- Full menu management — add, edit, delete items by category
- Accept/reject incoming orders
- Update order status in real time
- View daily/weekly order reports

### Delivery Driver Portal
- View assigned deliveries
- Update live GPS location (every 3 seconds)
- Update delivery status through the workflow
- View delivery history and earnings

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 17, Spring Boot 3.2, Spring Cloud Gateway |
| **Security** | Spring Security, JWT (jjwt 0.12.5) |
| **Messaging** | Apache Kafka 7.6 (Confluent) |
| **Caching** | Redis 7 (Spring Cache + RedisTemplate) |
| **Database** | MySQL 8 — database-per-service pattern |
| **ORM** | Spring Data JPA, Hibernate |
| **Frontend** | Angular 17, TypeScript, RxJS, Angular Material |
| **Real-time** | WebSocket (STOMP) for live order tracking |
| **API Docs** | Swagger / OpenAPI 3.0 |
| **Cloud** | AWS EC2, AWS RDS, AWS S3 |
| **DevOps** | Docker, Docker Compose, GitHub Actions CI/CD |
| **Utilities** | Lombok, MapStruct, Maven |

---

## 🗂️ Project Structure

```
quickbite/
├── api-gateway/              # Spring Cloud Gateway — JWT auth, routing, rate limiting
├── user-service/             # Registration, login, JWT generation, BCrypt
├── order-service/            # Order CRUD, Kafka producer, Redis cache
├── restaurant-service/       # Menu management, Redis menu cache
├── delivery-service/         # Driver assignment, Redis GPS tracking
├── payment-service/          # Kafka consumer, idempotent payment processing
├── notification-service/     # Kafka consumer, email notifications
├── docker-compose.yml        # MySQL + Redis + Kafka + Zookeeper + Kafka UI
├── schema.sql                # Complete MySQL schema — all 6 databases
└── README.md
```

---

## ⚙️ Microservices & Ports

| Service | Port | Database | Key Responsibilities |
|---|---|---|---|
| API Gateway | 8080 | — | JWT validation, routing, Redis rate limiting |
| User Service | 8081 | quickbite_users | Register, login, BCrypt, JWT |
| Order Service | 8082 | quickbite_orders | Place/track/cancel orders, Kafka producer |
| Restaurant Service | 8083 | quickbite_restaurants | Menus, availability, Redis cache |
| Delivery Service | 8084 | quickbite_delivery | Driver tracking, Redis GPS |
| Payment Service | 8085 | quickbite_payments | Kafka consumer, idempotency |
| Notification Service | 8086 | quickbite_notifications | Kafka consumer, email |

---

## 📨 Kafka Event Flow

```
Order Service  ──── order-placed ────►  Payment Service
                                    ►  Notification Service

Order Service  ──── order-updated ──►  Delivery Service
                                    ►  Notification Service

Payment Service ─── payment-success ►  Order Service (confirm)
                                    ►  Notification Service

Order Service  ──── order-cancelled ►  Notification Service
```

**Why Kafka?**
Placing an order triggers payment AND notification simultaneously without Order Service knowing about them. If Payment Service is temporarily down, Kafka holds the event — no orders are lost. This is loose coupling in action.

---

## 🔴 Redis Usage

| Key Pattern | Service | TTL | Purpose |
|---|---|---|---|
| `driver:{id}:location` | Delivery | 5 min | Live GPS — 20–50 writes/sec, MySQL can't handle this |
| `orders::{orderId}` | Order | Until evicted | Frequent reads, rare changes |
| `menus::{restaurantId}` | Restaurant | Until evicted | Thousands of reads, rare updates |
| `restaurants::all-open` | Restaurant | Until evicted | Homepage query — very hot |

---

## 🗄️ MySQL Schema Design

6 isolated databases — no cross-database foreign keys (database-per-service pattern):

- **quickbite_users** — users, user_addresses
- **quickbite_restaurants** — restaurants, menu_categories, menu_items, restaurant_reviews
- **quickbite_orders** — orders, order_items, order_status_history
- **quickbite_payments** — payments, refunds
- **quickbite_delivery** — deliveries, delivery_drivers
- **quickbite_notifications** — notifications

**Key design decisions:**
- `order_items` snapshots `unit_price` and `item_name` at order time — historical accuracy even if menu changes later
- `UNIQUE(order_id)` on payments table — DB-level idempotency prevents double charges
- `order_status_history` audit table — full timeline of every status change
- Computed column: `subtotal GENERATED ALWAYS AS (quantity * unit_price) STORED`

---

## 🔒 Security

JWT authentication is handled entirely at the **API Gateway** level:

1. User logs in → User Service issues a JWT signed with a secret key
2. Every subsequent request includes `Authorization: Bearer <token>`
3. API Gateway's `AuthenticationFilter` validates the JWT
4. Validated user identity is forwarded downstream as `X-User-Id` and `X-User-Role` headers
5. Downstream services trust these headers — no re-validation needed

Public endpoints (no JWT required): `/api/users/register`, `/api/users/login`, `GET /api/restaurants`

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Node.js 18+ (for Angular frontend)
- Docker & Docker Compose
- Maven 3.8+

### 1. Start Infrastructure

```bash
# Starts MySQL, Redis, Kafka, Zookeeper, Kafka UI
docker-compose up -d
```

### 2. Apply Database Schema

```bash
mysql -h 127.0.0.1 -u root -proot < schema.sql
```

### 3. Run Backend Services

Open 7 terminals and run each service:

```bash
cd api-gateway          && mvn spring-boot:run   # :8080
cd user-service         && mvn spring-boot:run   # :8081
cd order-service        && mvn spring-boot:run   # :8082
cd restaurant-service   && mvn spring-boot:run   # :8083
cd delivery-service     && mvn spring-boot:run   # :8084
cd payment-service      && mvn spring-boot:run   # :8085
cd notification-service && mvn spring-boot:run   # :8086
```

### 4. Run Angular Frontend

```bash
cd quickbite-frontend
npm install
ng serve
# Open http://localhost:4200
```

### 5. Monitor Kafka Events

Open **http://localhost:8090** — Kafka UI shows all topics and live messages.

---

## 🧪 API Testing — Quick Start

```bash
# 1. Register a customer
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123","fullName":"John Doe","role":"CUSTOMER"}'

# 2. Login — copy the token from the response
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"john@example.com","password":"password123"}'

# 3. Browse restaurants (no token needed)
curl http://localhost:8080/api/restaurants

# 4. View menu
curl http://localhost:8080/api/restaurants/1/menu

# 5. Place an order (replace <TOKEN>)
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "restaurantId": 1,
    "deliveryAddress": "123 Main Street, Pune",
    "items": [
      {"menuItemId": 1, "itemName": "Butter Chicken", "quantity": 2, "unitPrice": 320.00},
      {"menuItemId": 5, "itemName": "Butter Naan",    "quantity": 4, "unitPrice": 50.00}
    ]
  }'

# 6. Track your order
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8080/api/orders/1

# 7. Check payment status
curl -H "Authorization: Bearer <TOKEN>" http://localhost:8080/api/payments/order/1
```

---

## 📐 Design Patterns Used

| Pattern | Where used |
|---|---|
| **API Gateway** | Single entry point, JWT auth, rate limiting |
| **Database per service** | Each microservice owns its own MySQL database |
| **Event-driven architecture** | Kafka decouples Order → Payment → Notification |
| **CQRS (partial)** | Separate read (Redis cache) and write (MySQL) paths |
| **Factory pattern** | Service layer object construction |
| **Strategy pattern** | Dynamic pricing and payment method handling |
| **Observer pattern** | Kafka consumers react to events |
| **Builder pattern** | Entity construction via Lombok @Builder |
| **Idempotency** | Payment deduplication at application + DB level |

---

## ☁️ AWS Deployment

| Resource | Usage |
|---|---|
| **EC2** | Hosts all 7 Spring Boot microservices in Docker containers |
| **RDS (MySQL 8)** | 6 separate databases — one per service |
| **S3** | Restaurant images, menu item photos |
| **CloudWatch** | Logs and metrics monitoring for all services |

---

## 📊 Key Metrics

- **7** independent microservices
- **4** Kafka topics
- **6** isolated MySQL databases
- **4** Redis key patterns
- **15** MySQL tables with proper indexes
- **3** Angular portals
- **11** REST API groups

---

## 🤝 Connect

**Swapnil Udapure** — Backend Engineer | Java | Spring Boot | Microservices

- GitHub: [github.com/swapnil8585](https://github.com/swapnil8585)
- LinkedIn: [linkedin.com/in/swapnil-udapure](https://linkedin.com/in/swapnil-udapure)
- Email: swapniludapure53@gmail.com

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
