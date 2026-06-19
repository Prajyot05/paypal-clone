<div align="center">
  <img src="https://img.icons8.com/color/96/000000/globe-earth.png" alt="AtlasPay Logo"/>
  <h1>AtlasPay: Distributed Payment Processing System</h1>
  <p>An enterprise-grade, event-driven payment infrastructure built with <b>Java 21</b>, <b>Spring Boot</b>, <b>gRPC</b>, and <b>Kafka</b>.</p>

  <p>
    <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
    <img src="https://img.shields.io/badge/Spring_Boot-3.2+-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
    <img src="https://img.shields.io/badge/gRPC-244C5A?style=for-the-badge&logo=grpc&logoColor=white" />
    <img src="https://img.shields.io/badge/Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white" />
    <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" />
    <img src="https://img.shields.io/badge/PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white" />
  </p>
</div>

---

## 📖 Overview

AtlasPay is a highly scalable, distributed microservices architecture designed to tackle the hardest engineering problems in financial systems: **Concurrency**, **Idempotency**, and **Distributed Transactions**.

It relies on a hybrid communication model, utilizing **gRPC** for low-latency internal service-to-service communication, and REST for client-facing API Gateway routing. The system achieves true eventual consistency using an event-driven architecture powered by Kafka and the Transactional Outbox pattern.

## 🧠 Core Engineering Achievements

### 1. Hybrid API: REST + gRPC with Protocol Buffers
To satisfy modern microservice performance requirements:
* External clients (Frontend, Mobile) communicate with the API Gateway via standard REST JSON APIs.
* **Internal services communicate via gRPC.** For example, the `transaction-service` validates users against the `user-service` synchronously via highly efficient, binary Protocol Buffers. This hybrid approach allows public clients to use standard web protocols while internal infrastructure runs at maximum throughput.

### 2. Distributed Transactions (Saga Pattern) & Outbox Pattern
Financial systems cannot afford the locking overhead of 2-Phase Commits (2PC) or the data loss of dual-write failures.
* **Transactional Outbox Pattern:** When a transaction is saved to Postgres, an event is atomically saved to an `Outbox` table. A Polling Publisher reads this table and guarantees At-Least-Once delivery to Kafka, bypassing complex Debezium CDC requirements while maintaining data integrity.
* **Choreography Saga:** The `transaction-service` acts as the orchestrator. It waits for asynchronous Kafka events from both the `wallet-service` (funds transferred) and the `fraud-service` (velocity checked) before finalizing a transaction.

### 3. Fault Tolerance with Resilience4j
Google loves reliability. To prevent cascading failures across the distributed system, all synchronous gRPC calls are wrapped with **Resilience4j**.
* **Circuit Breakers** halt requests to failing downstream services.
* **Timeouts & Retries** gracefully handle transient network jitter, providing fallback mechanisms to maintain system uptime.

### 4. Concurrency Control & Absolute Idempotency
* **Pessimistic Locking:** The `wallet-service` utilizes `SELECT FOR UPDATE` (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) to lock sender and receiver rows deterministically, entirely preventing deadlocks during concurrent transfers.
* **Redis Idempotency:** The frontend generates a unique `Idempotency-Key` (UUID) per request. The `transaction-service` uses Redis `SET NX` to acquire a distributed lock, ensuring exactly-once processing even if the client retries the request.

## 🏗️ System Architecture

The ecosystem consists of 7 independent microservices utilizing the Database-per-Service pattern.

1. **`api-gateway` (Port 8080):** Spring Cloud Gateway for routing, JWT auth, and Redis Token Bucket rate limiting.
2. **`user-service` (Port 8081):** Manages auth. Exposes a gRPC server for internal validation.
3. **`transaction-service` (Port 8082):** Orchestrates Sagas, runs Outbox publisher, and uses Resilience4j gRPC clients.
4. **`wallet-service` (Port 8083):** Manages double-entry ledger bookkeeping.
5. **`fraud-service` (Port 8085):** Consumes Kafka events, runs Redis velocity checks, and blocks suspicious transactions.
6. **`analytics-service` (Port 8086):** Consumes all events to maintain throughput and volume metrics.
7. **`notification-service` (Port 8084):** Async email dispatcher.

## 🚀 How to Run Locally

### Prerequisites
* Docker & Docker Compose
* Java 21+ & Maven

### 1. Start Infrastructure
```bash
docker-compose up -d
```

### 2. Build and Run the Backend
```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
mvn clean install -DskipTests
```
Start all 7 Spring Boot microservices.

### 3. Start the Frontend
```bash
cd frontend
npm install
npm run dev
```
Navigate to **http://localhost:3000** in your browser.
