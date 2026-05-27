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

System Architecture
The workflow splits cleanly into an offline ad-hoc mesh layer and a traditional, high-throughput financial backend pipeline:

1. Conceptual Data Flow
[ OFFLINE ENVIRONMENT ]                                 [ ONLINE CENTRAL BACKEND ]
 
 +------------------+     Mesh Gossip / Hops     +---------------+               +---------------------+
 |   Sender Node    | -------------------------> |  Bridge Node  | ------------> | Ingestion Controller |
 | (Signs & Encrypts|      (BLE / Wi-Fi Direct)  | (Gains WAN/4G)|  HTTPS POST   +---------------------+
 +------------------+                            +---------------+                          |
                                                                                            v
 +------------------+                                                            +---------------------+
 | Relational DB    | <--- Atomic Database Transaction <------------------------- | Idempotency Check   |
 | Accounts/Ledger  |                                                            | (SHA-256 Cache Lock)|
 +------------------+                                                            +---------------------+
                                                                                            |
                                                                                            v
 +------------------+                                                            +---------------------+
 | Kafka Event Bus  | <--- Publish Settlement Event (Async Notification) -------- | Cryptographic Decrypt|
 +------------------+                                                            | (RSA Private Key)   |
                                                                                 +---------------------+
2. Deep-Dive Backend Ingestion Pipeline
Ingest & Hash: The backend receives an payload containment wrapper (MeshPacket). It immediately computes a SHA-256 digest of the immutable ciphertext.

Idempotency Gatekeeping: The digest is treated as a unique transaction token and claimed atomically via the IdempotencyService. If the token already exists in the lock-cache, it is dropped as a duplicate.

Envelope Decryption: The unique packet is handed to the HybridCryptoService. The system unwraps the ephemeral AES key using the server’s RSA private key, then decrypts the core payment instructions using AES-GCM-256. Any integrity validation failure drops the packet instantly.

Temporal Validation: The internal timestamp (signedAt) is evaluated. Packets older than the max age threshold are flagged as expired to thwart replay attempts.

Atomic Settlement: The decoupled instruction is handled inside a isolated @Transactional boundary where account balances are atomically verified, adjusted, and preserved into an immutable transaction ledger.

Core Infrastructure Stack
Runtime Engine: Java 21 & Spring Boot 3.5.4

Application Framework: Spring Web (REST API Management), Spring Data JPA (Data Abstraction)

Security & Cryptography: BouncyCastle Provider (bcprov-jdk18on)

Enterprise Message Bus: Apache Kafka (Asynchronous Event Distribution)

Storage & Caching Layer: PostgreSQL (Financial Ledger Records), Redis (Distributed Invalidation Cache)

User Interface: Thymeleaf (Interactive Simulation Management Dashboard)

Getting Started
Prerequisites
Java Development Kit (JDK): Version 21 or higher

Containerization Engine: Docker & Docker Compose (for rapid infrastructure provisioning)

Setup Environment
Clone the repository and navigate to the project root directory.

Spin up the prerequisite streaming and messaging services using Docker:

Bash
docker compose up -d
Note: By default, the application is configured to fall back to an in-memory database and local memory-map for idempotency tracking if direct environment variables for PostgreSQL and Redis are absent, making local onboarding simple.

Run the Server
Execute the platform using the bundled Maven Wrapper:

Bash
# Unix/macOS
./mvnw spring-boot:run

# Windows PowerShell
.\mvnw.cmd spring-boot:run
Once the log outputs Started OffupiApplication, access the simulation engine via your browser:
👉 http://localhost:8080

Run Verification Suite
Execute the integration test suite, which includes automated thread concurrency validations and cryptographic tampering test matrices:

Bash
./mvnw test
Detailed Step-by-Step Demo Flow
To easily witness the mechanics of the engine without external hardware, navigate to the web dashboard:

Generate Outbound Tx: Enter account parameters and trigger "Inject into Mesh". The system creates a PaymentInstruction, encrypts it using the server's public key, and stages it on a virtual isolated simulated device node.

Execute Gossip Matrix: Click "Run Gossip Round". You will observe the packet hop between simulated nodes in real time. Notice the Time-to-Live (TTL) counter decrement on each consecutive hop.

Trigger Gateway Uplink: Click "Flush Bridges". This action simulates an edge node transitioning back into cellular range. The edge node sends the accumulated packets to the server ingestion endpoint.

Observe Ledger Integrity: Inspect the Ledger logs at the base of the UI dashboard. To test the robustness of the system, inject a single transaction into multiple nodes simultaneously and execute a multi-bridge flush. The dashboard ledger will show exactly one successful mutation, proving the idempotency layer caught the duplicate transmissions.

Operational Architecture & Component Breakdown
src/main/java/com/example/OFFUPI/
├── config/                 # Infrastructure, Kafka Streams, and Cache configurations
├── controller/             # Simulation management endpoints and REST Entrypoints
├── crypto/                 # Low-level RSA/AES hybrid key wrappers & security logic
├── entity/                 # Account balances and strict ledger schemas
├── repository/             # Database access layers for state persistence
└── service/                # Core domain orchestration engine
    ├── BridgeIngestionService.java   # Evaluates, unlocks, and processes incoming mesh waves
    ├── IdempotencyService.java       # Atomic token verification (ConcurrentMap/Redis implementation)
    ├── HybridCryptoService.java     # Payload encryption and decryption engine
    ├── SettlementService.java       # Atomic ACID ledger state mutator
    └── MeshSimulatorService.java     # Background processing for virtual P2P gossip
Production Readiness: Gaps to Bridge
This repository serves as a functional demonstration architectural prototype. Transitioning this system to a commercial deployment requires the following modifications:

Component	Current Prototype State	Production Engineering Requirement
Idempotency Store	Monolithic Local ConcurrentHashMap	High-throughput distributed cache (e.g., Redis via atomic SETNX commands with strict TTL expirations).
Secret Management	Local memory initialization on booting sequence	Integration with a FIPS 140-2 Level 3 Hardware Security Module (HSM) or Cloud KMS provider.
Transport Layer	Emulated memory-swapping routine	Production mobile platform integration leveraging Android/iOS CoreBluetooth / Wi-Fi Aware APIs.
Ledger Audits	Traditional Relational DBMS Rows	Double-entry bookkeeping ledger using append-only immutable tables or cryptographic ledger databases.
Core Limitations of the Concept
Asymmetric Balance Trust: Because the receiver node cannot safely check the sender's central bank account balance while completely offline, transactions operate as cryptographically signed IOUs. This creates a risk of bad debts if the sender overdraws their account. To fix this, production environments must use localized pre-funded balances or hardware-enforced secure elements.

Race Conditions on Double-Spending: If a sender constructs two different transactions using the same funds and routes them through two separate physical directions, the transaction that reaches the online backend first will be cleared, causing the second transaction to fail upon arrival.

Troubleshooting
Database Port Violations: If you run into database user authentication issues or port conflicts, confirm no local instances of PostgreSQL or Redis are conflicting with your Docker containers, or update the src/main/resources/application.properties settings.

Container Networking Loops: If Kafka fails to boot cleanly, ensure Docker Desktop has allocated at least 2GB of active RAM to your environment.