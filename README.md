# 🛒 ShopCart — System Architecture

## Architecture Overview

```mermaid
flowchart TD
    Browser["🌐 User Browser\nReact SPA"]

    subgraph EC2["☁️ AWS EC2 Instance (Docker Containers)"]

        Nginx["⚙️ Nginx\nReverse Proxy · Static React Files"]

        subgraph Backend["Spring Boot Backend"]
            direction TB

            subgraph Controllers["Controller Layer"]
                C1["AuthController"]
                C2["ProductController"]
                C3["CategoryController"]
                C4["CartController"]
                C5["OrderController"]
                C6["DiscountController"]
                C7["UserController"]
            end

            subgraph Security["Security Layer"]
                S1["JWT Filter"]
                S2["JWT Token Provider"]
                S3["Spring Security"]
                S4["Role Authorization"]
            end

            subgraph Services["Service Layer"]
                SV1["Auth Service"]
                SV2["Product Service"]
                SV3["Category Service"]
                SV4["Cart Service"]
                SV5["Order Service"]
                SV6["Discount Service"]
                SV7["User Service"]
            end

            subgraph Persistence["Persistence Layer"]
                P1["JPA Repositories"]
                P2["Hibernate ORM"]
                P3["MySQL Queries"]
            end

            Controllers --> Security --> Services --> Persistence
        end

        Logging["📋 SLF4J + AOP Logging\nAPI Requests · Execution Time · Exceptions\nAuthentication Events · Business Operations"]
    end

    subgraph AWS["☁️ AWS Managed Services"]
        RDS[("🗄️ AWS RDS — MySQL\nUsers · Roles · Products · Categories\nDiscounts · Cart · Cart Items\nOrders · Order Items · Reviews")]
        CW["📊 AWS CloudWatch\nApplication Logs · Error Monitoring\nDocker Logs · EC2 Metrics\nRDS Metrics · Alerts"]
    end

    Browser -- "HTTPS" --> Nginx
    Nginx -- "/api/*" --> Backend
    Persistence -- "JDBC" --> RDS
    Backend --> Logging
    Logging --> CW
    RDS --> CW
```

---

## Layer Breakdown

| Layer | Components | Responsibility |
|---|---|---|
| **Frontend** | React SPA | UI, routing, state management |
| **Reverse Proxy** | Nginx | SSL termination, static file serving, API routing |
| **Controller** | 7 REST controllers | HTTP request handling, input validation |
| **Security** | JWT + Spring Security | Authentication, role-based authorization |
| **Service** | 7 business services | Core business logic, transaction management |
| **Persistence** | JPA / Hibernate | ORM, database queries |
| **Database** | AWS RDS MySQL | Persistent data storage (10 tables) |
| **Logging** | SLF4J + AOP | Cross-cutting observability, audit trail |
| **Monitoring** | AWS CloudWatch | Metrics, logs, alerting |

---

## Security — Role Hierarchy

```
SUPER_ADMIN  ──▶  full platform access
    ADMIN    ──▶  product, category, discount, order management
     USER    ──▶  browse, cart, checkout, reviews
```

---

## Database Schema (Tables)

```
Users · Roles · Products · Categories · Discounts
Cart · Cart Items · Orders · Order Items · Reviews
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React.js |
| Backend | Spring Boot (Java) |
| Authentication | JWT (JSON Web Tokens) |
| ORM | Hibernate / JPA |
| Database | MySQL on AWS RDS |
| Web Server | Nginx |
| Containerization | Docker |
| Hosting | AWS EC2 |
| Logging | SLF4J + Spring AOP |
| Monitoring | AWS CloudWatch |
