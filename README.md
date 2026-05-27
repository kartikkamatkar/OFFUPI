# 🌐 UPI Offline Mesh — OFFUPI

> An enterprise-grade Spring Boot demonstration of an offline, asynchronous, peer-to-peer payment routing engine.

---

## 📋 Overview

**OFFUPI** solves the **"zero-connectivity ledger problem."**

Imagine a user in a remote area or basement with absolutely no cellular network. They can still initiate a payment to a peer. The transaction is securely encrypted, broadcasted over a local device-to-device network, and hops across intermediate nodes until a single device establishes an internet connection and relays the payload to the central backend for atomic settlement.

This repository contains the core Spring Boot backend alongside an in-memory network simulator, allowing you to validate end-to-end cryptographic integrity, network gossip, and distributed idempotency on a single development machine.

---

## ✨ Technical Capabilities Demonstrated

• **🔒 Zero-Trust Relay Integrity** — Transactions traverse completely untrusted intermediary nodes without exposure or risk of tampering, enforced via an asymmetric/symmetric hybrid encryption layer (RSA-OAEP + AES-256-GCM).

• **🔄 Distributed Idempotency (Deduplication)** — Prevents duplicate-packet storms inherent to mesh networks. The backend ensures that even if a single packet is broadcasted by dozens of bridge nodes simultaneously, the ledger settles exactly once.

• **⛔ Replay & Tamper Defenses** — Cryptographic signatures and explicit temporal windows reject manipulated, stale, or replayed packets before they ever interact with the core banking database.

---

## 🏗️ System Architecture

The workflow splits cleanly into an offline ad-hoc mesh layer and a traditional, high-throughput financial backend pipeline.

### 1️⃣ Conceptual Data Flow

```
[ OFFLINE ENVIRONMENT ]                              [ ONLINE CENTRAL BACKEND ]

 +------------------+    Mesh Gossip/Hops    +---------------+               +---------------------+
 |  Sender Node     | ──────────────────────→ | Bridge Node   | ────────────→ | Ingestion Controller|
 | (Signs & Encrypts|   (BLE / Wi-Fi Direct)  | (Gains 4G)    |  HTTPS POST   +---------------------+
 +------------------+                         +---------------+                         │
                                                                                        ↓
 +------------------+                                                        +---------------------+
 | Account Ledger   | ←─── Atomic DB Transaction ←───────────────────────── | Idempotency Check   |
 | & Balances       |                                                        | (SHA-256 Cache Lock)|
 +------------------+                                                        +---------------------+
                                                                                        │
                                                                                        ↓
 +------------------+                                                        +---------------------+
 | Kafka Event Bus  | ←─ Settlement Event ←────────────────────────────────── | Crypto Decrypt      |
 | (Notifications)  |                                                        | (RSA Private Key)   |
 +------------------+                                                        +---------------------+
```

### 2️⃣ Deep-Dive Backend Ingestion Pipeline

1. **📥 Ingest & Hash** — The backend receives a payload wrapper (MeshPacket). It immediately computes a SHA-256 digest of the immutable ciphertext.

2. **🔐 Idempotency Gatekeeping** — The digest is treated as a unique transaction token and claimed atomically via `IdempotencyService`. If the token already exists in the lock-cache, it is dropped as a duplicate.

3. **🔓 Envelope Decryption** — The unique packet is handed to `HybridCryptoService`. The system unwraps the ephemeral AES key using the server's RSA private key, then decrypts the core payment instructions using AES-GCM-256. Any integrity validation failure drops the packet instantly.

4. **⏰ Temporal Validation** — The internal timestamp (`signedAt`) is evaluated. Packets older than the max age threshold are flagged as expired to thwart replay attempts.

5. **✅ Atomic Settlement** — The decoupled instruction is handled inside an isolated `@Transactional` boundary where account balances are atomically verified, adjusted, and preserved into an immutable transaction ledger.

---

## ⚙️ Core Infrastructure Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Runtime Engine** | Java 21 & Spring Boot 3.5.4 | Application runtime & framework |
| **Web Framework** | Spring Web (REST API) | HTTP endpoints & dashboard serving |
| **Data Layer** | Spring Data JPA | Object-relational mapping |
| **Cryptography** | BouncyCastle (bcprov-jdk18on) | RSA-OAEP & AES-256-GCM encryption |
| **Message Bus** | Apache Kafka | Asynchronous event distribution |
| **Primary DB** | PostgreSQL (JDBC) | Financial ledger & account records |
| **Cache Layer** | Redis | Distributed idempotency store (optional) |
| **Templates** | Thymeleaf | Interactive simulation dashboard |
| **Observability** | Micrometer + Prometheus | Metrics collection & scraping |
| **Build Tool** | Maven Wrapper (`mvnw`) | Dependency & build management |

---

## 🚀 Getting Started

### ✅ Prerequisites

• **Java Development Kit (JDK)** — Version 21 or higher
  ```bash
  java -version
  ```

• **Docker & Docker Compose** — For rapid infrastructure provisioning
  ```bash
  docker --version
  ```

### 📦 Setup Environment

1. **Clone and navigate to the project:**
   ```bash
   cd /path/to/OFFUPI
   ```

2. **Spin up prerequisite services (Kafka, Zookeeper):**
   ```bash
   docker-compose up -d
   ```
   > Note: The application falls back to in-memory DB and local memory-map for idempotency if PostgreSQL/Redis env vars are absent.

3. **Start the backend server:**

   **Unix/macOS:**
   ```bash
   ./mvnw spring-boot:run
   ```

   **Windows PowerShell:**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

4. **Access the dashboard:**
   ```
   👉 http://localhost:8080
   ```
   Once you see `Started OffupiApplication` in the logs, open your browser.

5. **Stop the server:**
   ```bash
   Ctrl+C
   ```

### 🧪 Run Verification Suite

Execute the integration test suite (includes concurrency validation & crypto tampering tests):

```bash
./mvnw test
```

---

## 📊 Detailed Step-by-Step Demo Flow

To easily witness the mechanics without external hardware, navigate to the web dashboard:

### 1️⃣ Generate Outbound Transaction

• Click **"📤 Inject into Mesh"**
• Enter account parameters (sender, receiver, amount)
• The system creates a `PaymentInstruction`, encrypts it using the server's public key, and stages it on a virtual simulated device node.

### 2️⃣ Execute Gossip Matrix

• Click **"🔄 Run Gossip Round"** (repeat as needed)
• Observe the packet hop between simulated nodes in real time.
• Notice the **Time-to-Live (TTL)** counter decrement on each consecutive hop.

### 3️⃣ Trigger Gateway Uplink

• Click **"📡 Flush Bridges"**
• This action simulates an edge node transitioning back into cellular range.
• The edge node sends accumulated packets to the server ingestion endpoint.

### 4️⃣ Observe Ledger Integrity

• Inspect the **Ledger logs** at the base of the UI dashboard.
• To test robustness: inject a single transaction into multiple nodes simultaneously and execute a multi-bridge flush.
• The dashboard ledger will show **exactly one successful mutation**, proving the idempotency layer caught duplicates. ✅

---

## 📁 Operational Architecture & Component Breakdown

```
src/main/java/com/example/OFFUPI/
├── config/                 # 🔧 Infrastructure, Kafka Streams, & Cache configurations
├── controller/             # 🌐 Simulation management endpoints & REST entrypoints
├── crypto/                 # 🔐 RSA/AES hybrid key wrappers & security logic
├── entity/                 # 💾 Account balances & strict ledger schemas
├── repository/             # 🗂️ Database access layers for state persistence
├── dto/                    # 📦 Data transfer objects (SendMoneyRequest, PaymentEvent)
├── event/                  # 📨 Event domain objects
├── kafka/                  # 📡 Consumer & producer implementations
└── service/                # ⚙️ Core domain orchestration engine
    ├── BridgeIngestionService.java   # 🚪 Evaluates, unlocks, & processes incoming mesh waves
    ├── IdempotencyService.java       # 🔒 Atomic token verification (ConcurrentMap/Redis)
    ├── HybridCryptoService.java      # 🔐 Payload encryption & decryption engine
    ├── SettlementService.java        # ✅ Atomic ACID ledger state mutator
    ├── MeshSimulatorService.java     # 🌐 Background processing for virtual P2P gossip
    ├── DemoService.java              # 🎮 Demo helper & virtual device orchestration
    └── MetricsService.java           # 📈 Observability & metrics tracking
```

---

## 📋 Production Readiness: Gaps to Bridge

This repository serves as a **functional demonstration & architectural prototype**. Transitioning to production requires:

| Component | Current Prototype | Production Requirement |
|-----------|------------------|----------------------|
| **Idempotency Store** | Monolithic Local `ConcurrentHashMap` | 🔴 High-throughput distributed cache (Redis with atomic SETNX + TTL) |
| **Secret Management** | Local memory initialization on startup | 🔴 FIPS 140-2 Level 3 HSM or Cloud KMS provider |
| **Transport Layer** | Emulated memory-swapping routine | 🔴 Production mobile (Android/iOS CoreBluetooth / Wi-Fi Aware APIs) |
| **Ledger Audits** | Traditional Relational DBMS rows | 🔴 Double-entry bookkeeping or append-only immutable tables |
| **Authentication** | None (demo only) | 🔴 Mutual TLS or signed bridge-node certificates |
| **Rate Limiting** | None | 🔴 Per-bridge-node + per-sender velocity checks |

---

## ⚠️ Core Limitations of the Concept

• **❌ Asymmetric Balance Trust** — The receiver node cannot safely check the sender's central bank account balance while completely offline. Transactions operate as cryptographically signed IOUs. Production must use localized pre-funded balances or hardware-enforced secure elements.

• **❌ Race Conditions on Double-Spending** — If a sender constructs two different transactions using the same funds and routes them through two separate directions, the transaction reaching the backend first will clear; the second will fail upon arrival.

• **❌ Real-World BLE Challenges** — Background BLE on Android is heavily throttled since Android 8. iOS peripheral mode is locked down. Real-world device-to-device mesh is a hard engineering problem and is not addressed in this simulator.

---

## 🔧 Troubleshooting

| Issue | Solution |
|-------|----------|
| **`FATAL: password authentication failed for user "${DB_NAME}"`** | Set `DB_NAME` and `DB_PASS` env vars or edit `application.properties` to point to a reachable Postgres instance. |
| **Port 8080 already in use** | Change `server.port` in `src/main/resources/application.properties` |
| **Docker networking issues** | Ensure Docker Desktop/Engine has ≥2GB RAM allocated. Run `docker-compose logs` to debug. |
| **Kafka fails to boot** | Verify no local Kafka instance conflicts. Check `docker ps` and clean up stale containers. |
| **First mvnw run hangs** | Maven is downloading dependencies (~80 MB). Give it 2–3 minutes on a normal connection. |
| **Tests fail intermittently** | Concurrency tests are timing-sensitive. Run 3x; if persistent, report output. |

---

## 📜 License

**Demo code** — no license included. Use for learning and experiments. 📚

---

## 🎯 Next Steps

Choose one of the following to extend the project:

• **Add Postgres + Redis services to `docker-compose.yml`** for a complete one-command stack
• **Insert beginner-friendly inline comments** into main Java service files
• **Build mobile client** (Android/iOS) to test real device-to-device mesh
• **Integrate with real UPI backends** for production-grade settlement
• **Add comprehensive API documentation** (Swagger/OpenAPI)

Reach out with questions or feedback! 🚀
