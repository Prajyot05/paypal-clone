<div align="center">
  <img src="https://img.icons8.com/color/96/000000/paypal.png" alt="PayPal Logo"/>
  <h1>Enterprise-Grade PayPal Clone</h1>
  <p>A production-ready distributed payment system built with <b>Java 21</b>, <b>Spring Boot</b>, and <b>Kafka</b>.</p>

  <p>
    <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
    <img src="https://img.shields.io/badge/Spring_Boot-3.2+-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white" />
    <img src="https://img.shields.io/badge/Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white" />
    <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white" />
    <img src="https://img.shields.io/badge/PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white" />
    <img src="https://img.shields.io/badge/Next.js-000000?style=for-the-badge&logo=next.js&logoColor=white" />
  </p>
</div>

---

## 📖 Overview

This repository is not just a CRUD application. It is a highly scalable, distributed microservices architecture designed to tackle the hardest engineering problems in financial systems: **Concurrency**, **Idempotency**, and **Distributed Transactions**.

Built as a full-stack monorepo, the backend relies on an event-driven architecture using Kafka, while the frontend provides a sleek, modern UI built with Next.js and Tailwind CSS to vividly demonstrate real-time eventual consistency.

## 🧠 Core Engineering Achievements

This project was built to demonstrate an understanding of complex distributed system patterns commonly required at top-tier tech companies like Google.

### 1. Distributed Transactions (Saga Pattern)
Financial systems cannot afford the locking overhead of 2-Phase Commits (2PC). This system implements the **Choreography-based Saga Pattern** using Apache Kafka. 
* When a user initiates a transfer, the `transaction-service` creates a `PENDING` record.
* It publishes a `txn-initiated` event to Kafka.
* The `wallet-service` consumes this event, processes the transfer atomically, and responds with a `txn-completed` or `txn-failed` event.
* The frontend polls the transaction status, providing a beautiful visual representation of eventual consistency.

### 2. Concurrency Control & Race Condition Prevention
Preventing deadlocks and race conditions during simultaneous money transfers is critical. The `wallet-service` utilizes **Pessimistic Locking** (`SELECT FOR UPDATE`) at the database level (`@Lock(LockModeType.PESSIMISTIC_WRITE)`).
* Sender and receiver rows are locked in a deterministic, consistent order to completely eliminate the possibility of database deadlocks when concurrent transfers occur between the same users.

### 3. Absolute Idempotency via Redis
Network retries or a user double-clicking the "Send" button should never result in a double charge. 
* The frontend generates a unique `UUID` for every transfer request.
* The API Gateway forwards this `Idempotency-Key` to the `transaction-service`.
* The service uses **Redis `setIfAbsent` (SET NX)** to acquire a distributed lock on that specific key. Duplicate requests are intercepted instantly at the cache layer and rejected, ensuring exactly-once processing.

### 4. Microservice Security & Rate Limiting
* **Spring Cloud Gateway** acts as the single entry point.
* It implements a global **JWT Authentication Filter** that intercepts and validates tokens, forwarding the user identity (`X-Authenticated-User`) to downstream microservices.
* **Redis-backed Rate Limiting** is applied globally at the API Gateway level to mitigate DDoS attacks and API abuse using the Token Bucket algorithm.

## 🏗️ System Architecture

The ecosystem consists of 5 independent microservices utilizing the Database-per-Service pattern.

1. **`api-gateway` (Port 8080):** Spring Cloud Gateway for routing, auth, and rate limiting.
2. **`user-service` (Port 8081):** Manages user registration/authentication. Has its own Postgres DB.
3. **`wallet-service` (Port 8083):** Manages user balances and maintains an append-only `WalletLedgerEntry` for double-entry bookkeeping.
4. **`transaction-service` (Port 8082):** Orchestrates the Saga pattern and handles Redis idempotency.
5. **`notification-service` (Port 8084):** Consumes Kafka events asynchronously to dispatch notifications without blocking the main transaction flow.
6. **`frontend`:** Next.js (App Router) web app running on port 3000.

## 🚀 How to Run Locally

### Prerequisites
* Docker & Docker Compose
* Java 21+
* Node.js & npm

### 1. Start Infrastructure
The `docker-compose.yml` file contains all necessary infrastructure (Kafka, Zookeeper, Redis, PostgreSQL, Zipkin, Prometheus).
```bash
docker-compose up -d
```

### 2. Build and Run the Backend
Ensure your `JAVA_HOME` is set to Java 21, then compile all microservices:
```bash
mvn clean install -DskipTests
```
Start all 5 microservices. You can do this via your IDE or by running `mvn spring-boot:run` inside each respective module folder (`user-service`, `api-gateway`, etc).

### 3. Start the Frontend
```bash
cd frontend
npm install
npm run dev
```
Navigate to **http://localhost:3000** in your browser.

## 📜 License
This project is licensed under the MIT License.
