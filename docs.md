# OFFUPI Payment System - Complete Technical Documentation

> **SINGLE COMPREHENSIVE DOCUMENT** (Text Only - No Code)  
> This document contains EVERYTHING about the OFFUPI project in readable text format.

---

## TABLE OF CONTENTS

1. [Quick Reference Cheat Sheets](#quick-reference-cheat-sheets)
2. [Project Overview](#project-overview)
3. [Technology Stack Explained](#technology-stack-explained)
4. [Complete File Explanations](#complete-file-explanations)
5. [All Annotations Explained](#all-annotations-explained)
6. [Database Design](#database-design)
7. [API Documentation](#api-documentation)
8. [Complete Request Flow](#complete-request-flow)
9. [Architecture Diagrams (ASCII)](#architecture-diagrams-ascii)
10. [Design Patterns Used](#design-patterns-used)
11. [Interview Preparation (Q&A)](#interview-preparation-qa)
12. [Project Viva Guide](#project-viva-guide)
13. [Resume Section](#resume-section)
14. [Lessons Learned & Improvements](#lessons-learned--improvements)

---


---

## PART 1: QUICK REFERENCE CHEAT SHEETS

### What is OFFUPI? (One Line)
OFFUPI is an offline payment system that allows people to send money without internet by having phones relay encrypted messages like a chain.

### Technology Stack Cheat Sheet

| Category | Technology | What It Does |
|----------|-----------|--------------|
| Language | Java 21 | Main programming language |
| Framework | Spring Boot 3 | Web server, database, Kafka integration |
| Database | PostgreSQL 16 | Stores accounts and transactions permanently |
| Cache | Redis 7 | Prevents duplicate payments (super fast) |
| Message Queue | Apache Kafka | Handles payments asynchronously |
| Encryption | RSA + AES | Keeps payment data secret |
| Containers | Docker | Runs everything with one command |
| Monitoring | Prometheus + Grafana | Shows graphs of system health |

### Port Numbers Cheat Sheet (Where to Access)

| What | Port | How to Access |
|------|------|---------------|
| Web Application | 8081 | http://localhost:8081 |
| Database | 5433 | jdbc:postgresql://localhost:5433 |
| Redis Cache | 6380 | redis://localhost:6380 |
| Kafka Queue | 9093 | kafka://localhost:9093 |
| Prometheus Metrics | 9091 | http://localhost:9091 |
| Grafana Dashboards | 3001 | http://localhost:3001 |

### API Endpoints Cheat Sheet

| Method | URL | What It Does |
|--------|-----|--------------|
| GET | /api/accounts | Get all bank accounts |
| POST | /api/demo/send | Send a payment (demo) |
| GET | /api/mesh/state | See all phones in network |
| POST | /api/mesh/gossip | Phones share packets |
| POST | /api/mesh/flush | Upload to backend |
| POST | /api/mesh/reset | Clear all packets |
| GET | /api/transactions | See last 20 payments |
| GET | / | Open dashboard page |

### Common Annotations Cheat Sheet

| Annotation | Meaning |
|------------|---------|
| @SpringBootApplication | "This starts the whole application" |
| @RestController | "This handles web requests and returns JSON" |
| @Service | "This contains business logic" |
| @Repository | "This talks to the database" |
| @Entity | "This is a database table" |
| @Autowired | "Spring, please give me this dependency" |
| @Transactional | "All database operations succeed or all fail" |
| @KafkaListener | "Listen to Kafka messages" |
| @PostConstruct | "Run this after object is created" |

---

## PART 2: PROJECT OVERVIEW

### The Problem This Solves

Millions of people in rural areas, on airplanes, in submarines, or during natural disasters have **NO internet connection**. But they still need to send money.

Current payment systems like UPI, Credit Cards, PayPal **ALL require internet**. If you don't have internet, you cannot pay.

### The Solution

OFFUPI creates a **"mesh network"** of phones. Think of it like a chain of people passing notes:

1. You write payment details on a digital "note" (encrypted so nobody can read it)
2. You hand it to a nearby phone (even a stranger's phone)
3. That phone passes it to another phone
4. The note keeps moving through the chain
5. Eventually, someone reaches an area with internet
6. That person uploads the note to the bank
7. Bank processes your payment

### How It Works (Simple Version)

```
**Step  You create a payment on your phone (no internet needed)
        ↓
**Step  Your phone encrypts the payment (locks it in a digital envelope)
        ↓
**Step  Your phone sends it to nearby phones (Bluetooth/WiFi Direct)
        ↓
**Step  Phones keep passing it to each other (like a chain)
        ↓
**Step  A phone with internet receives it (Bridge Node)
        ↓
**Step  That phone uploads to the backend server
        ↓
**Step  Server decrypts and processes the payment
        ↓
**Step  Money is transferred!
```

### Key Features Explained

- **Offline Payments**: You don't need internet to SEND money. Only the final upload needs internet.
- **Mesh Networking**: Phones relay messages to each other. No single point of failure.
- **Hybrid Encryption**: Uses two types of encryption together. AES is fast (encrypts big data), RSA is secure (shares keys safely). This is what HTTPS uses!
- **Idempotency**: Prevents duplicate payments. Even if same packet arrives twice, it's processed only once.
- **Async Processing**: When a packet arrives, we don't process it immediately. We put it in Kafka queue and process later. This handles traffic spikes.
- **Retry Mechanism**: If processing fails (database down), we try again automatically.
- **Dead Letter Queue**: If payment fails permanently (bad PIN), it goes to DLQ for manual review.

---

## PART 3: TECHNOLOGY STACK EXPLAINED

### Why Java 21?

Java is the most popular language for enterprise applications. Java 21 is the latest Long-Term Support (LTS) version, meaning it will be supported for years. It has virtual threads which help handle many connections efficiently.

### Why Spring Boot?

Spring Boot is a framework that makes Java development easier. It automatically configures everything. Want a web server? Add one dependency and it works. Want database? Add another dependency. Spring Boot handles all the boring setup.

### Why PostgreSQL?

PostgreSQL is a reliable, free database that guarantees ACID properties. ACID means: Atomic (all or nothing), Consistent (rules enforced), Isolated (transactions don't interfere), Durable (data survives crashes). For money, we need all of these!

### Why Redis?

Redis stores data in RAM (computer memory) instead of hard drive. RAM is thousands of times faster. We use Redis to store which packet hashes we've already seen. Checking Redis takes microseconds; checking database takes milliseconds.

### Why Kafka?

Kafka is a message queue that can handle millions of messages. It stores messages on disk, so even if the consumer crashes, messages aren't lost. It also allows replaying messages. When we get a payment, we put it in Kafka and return immediately. The actual processing happens later.

### Why Hybrid Encryption (RSA + AES)?

RSA alone can only encrypt about 245 bytes (too small for our payment data). AES alone is fast but needs a shared key (how do we share it safely?). Solution: AES encrypts the payment (fast, any size), RSA encrypts the AES key (secure). Best of both worlds!

### Why Docker?

Docker packages the application and all its dependencies into a container. With one command (`docker-compose up`), we start: PostgreSQL, Redis, Kafka, Zookeeper, Prometheus, Grafana, AND our app. No manual installation needed!

---

## PART 4: COMPLETE FILE EXPLANATIONS

### CONFIGURATION PACKAGE (config/)

#### CorsConfig.java

#### KafkaConfig.java
**What it does**: Creates Kafka topics before the application uses them.

**Why it exists**: Kafka topics must exist before we send messages to them.

**When it's used**: At application startup.

**Who calls it**: Spring Boot's Kafka auto-configuration.

**What it returns**: NewTopic objects for each topic.

**Real-world analogy**: Creating mailboxes before mail arrives: "Incoming", "Retry", "Dead Letter".

The topics created are:

payment-ingestion: New payments arrive here first

payment-settled: Successfully processed payments

payment-invalid: Permanently failed payments

payment-retry: Temporarily failed payments (will retry)

payment-dead-letter: Failed after all retries (needs manual review)

#### RedisConfig.java
**What it does**: Creates a Redis connection template for string operations.

**Why it exists**: We need a way to talk to Redis from Java code.

**When it's used**: At startup, then whenever IdempotencyService needs Redis.

**Who calls it**: Spring Boot creates the bean; IdempotencyService uses it.

**What it returns**: StringRedisTemplate ready to use.

**Real-world analogy**: Creating a notebook and pen to write down tasks we've completed. Before doing a task, check notebook - if not there, do it and write it down.

### CONTROLLER PACKAGE (controller/)
#### AccountController.java
**What it does**: Handles API requests for account information.

**Why it exists**: Frontend needs to display account balances.

**When it's used**: When user visits accounts page.

**Who calls it**: Frontend JavaScript making HTTP GET request.

**What it returns**: List of all accounts as JSON.

**Real-world analogy**: Bank teller at the information counter. Customer asks "Show me all accounts" - teller fetches records.

**API Endpoint**: GET /api/accounts

#### DemoController.java
**What it does**: Demonstrates the complete payment flow.

**Why it exists**: To test the system without building a mobile app.

**When it's used**: When frontend demo page calls the send endpoint.

**Who calls it**: Demo frontend page or API testing tool.

**What it returns**: Success message string.

**Real-world analogy**: Bank demonstration counter showing how to deposit money.

**API Endpoint**: POST /api/demo/send
**Request Body**: {"senderVpa":"alice@demo", "receiverVpa":"bob@demo", "amount":100, "pin":"1234"}

#### HomeController.java
**What it does**: Serves the main dashboard HTML page.

**Why it exists**: Users need a visual interface, not just raw APIs.

**When it's used**: When user visits the root URL.

**Who calls it**: Web browser.

**What it returns**: Name of HTML template (dashboard.html).

**Real-world analogy**: Receptionist who hands you the main brochure when you enter.

**API Endpoint**: GET /

#### MeshController.java
**What it does**: Controls the simulated mesh network.

**Why it exists**: For administrators to monitor and control the mesh simulation.

**When it's used**: When admin calls mesh management endpoints.

**Who calls it**: Admin panel or API testing tool.

**What it returns**: State information or status messages.

**Real-world analogy**: Air traffic controller monitoring message flow.

API Endpoints:

GET /api/mesh/state - See all devices

POST /api/mesh/gossip - Phones share packets

POST /api/mesh/flush - Upload to backend

POST /api/mesh/reset - Clear everything

#### TransactionController.java
**What it does**: Shows recent payment transactions.

**Why it exists**: Users need to see payment history.

**When it's used**: When dashboard page loads.

**Who calls it**: Frontend dashboard JavaScript.

**What it returns**: Last 20 transactions as JSON.

**Real-world analogy**: Bank statement printer showing last 20 transactions.

**API Endpoint**: GET /api/transactions

### CRYPTO PACKAGE (crypto/)
#### HybridCryptoService.java
**What it does**: Handles encryption and decryption of payment data.

**Why it exists**: Payments must be secure - nobody should read or modify them.

**When it's used**: When phone creates packet (encrypts) and when server receives packet (decrypts).

**Who calls it**: DemoService (encrypt), AsyncSettlementService (decrypt).

**What it returns**: Encrypted string or decrypted PaymentInstruction.

**Real-world analogy**: A sealed envelope (encryption) and a letter opener (decryption).

**How hybrid encryption works**:

Generate random AES key for this payment only

Encrypt payment data with AES (fast, any size)

Encrypt the AES key with server's RSA public key (secure)

Package both together and send

Server decrypts AES key with RSA private key

Server decrypts payment with AES key

This is exactly how HTTPS, PGP, and Signal work!

#### ServerKeyHolder.java
**What it does**: Holds the server's RSA key pair (public and private keys).

**Why it exists**: Server needs private key to decrypt payments. Public key shared with clients.

**When it's used**: Generated at startup, used for every encryption/decryption.

**Who calls it**: HybridCryptoService.

**What it returns**: PublicKey or PrivateKey objects.

**Real-world analogy**: A locked mailbox with a slot (public key) and a key (private key). Anyone can drop letters in, only owner can open and read.

**Security note**: In production, private key would be stored in HSM (Hardware Security Module) or KMS (like AWS KMS). NEVER in code!

### DTO PACKAGE (dto/)
#### SendMoneyRequest.java
**What it does**: Data Transfer Object for payment requests from frontend.

**Why it exists**: Separates API contract from internal entity objects. We don't expose database entities directly to clients.

**When it's used**: When frontend sends POST request to /api/demo/send.

**Who calls it**: Spring Boot automatically creates object from JSON.

**What it contains**: senderVpa, receiverVpa, amount, pin.

**Real-world analogy**: A form that customer fills out at the bank counter.

**Why BigDecimal for amount**: Double has rounding errors (0.1 + 0.2 = 0.30000000000000004!). BigDecimal gives exact results - essential for money.

### ENTITY PACKAGE (entity/)
#### Account.java
**What it does**: Represents a bank account in the database. Maps to "accounts" table.

**Why it exists**: Store user account information (VPA, balance, etc.).

**When it's used**: Every time we read or write account data.

**Who calls it**: SettlementService (transfer money), AccountController (view).

**What it contains**: vpa (primary key), holderName, balance, version.

**Real-world analogy**: A row in a bank's ledger book.

**What is @Version: Optimistic locking prevents lost updates. If two people try to withdraw from same account at same time, only one succeeds. The other gets an error and must retry.

#### MeshPacket.java
**What it does**: Represents a packet traveling through the mesh network.

**Why it exists**: Packets need to carry data plus routing information.

**When it's used**: Created by DemoService, passed through MeshSimulatorService, processed by BridgeIngestionService.

**What it contains**: packetId (unique ID), ttl (Time To Live - hops remaining), createdAt (timestamp), ciphertext (encrypted payment).

**Real-world analogy**: A physical envelope traveling through postal system. Has tracking number, stamps left, sealed letter inside.

What is TTL: Prevents infinite loops. Each hop decreases TTL by 1. When TTL reaches 0, packet stops. Example: TTL=5 means max 5 phones can relay it.

#### PaymentInstruction.java
**What it does**: Represents decrypted payment instruction from a user.

**Why it exists**: Contains actual payment details after decryption.

**When it's used**: After packet is decrypted by server.

**Who calls it**: HybridCryptoService (decrypt returns this).

**What it contains**: senderVpa, receiverVpa, amount, pinHash (hashed PIN, not plain PIN!), nonce (unique ID, prevents replay attacks), signedAt (timestamp).

**Real-world analogy**: The letter inside the envelope after opening it.

#### Transaction.java
**What it does**: Permanent record of processed payment in database.

**Why it exists**: Audit trail and duplicate prevention.

**When it's used**: After payment is settled or rejected.

**Who calls it**: SettlementService.

**What it contains**: id (auto-increment), packetHash (unique - prevents duplicates), senderVpa, receiverVpa, amount, signedAt (when user created), settledAt (when server processed), bridgeNodeId (which bridge delivered), hopCount (how many hops), status (SETTLED or REJECTED).

**Real-world analogy**: Bank statement entry showing what happened.

**Why packetHash is unique**: Even if same packet arrives twice, database rejects duplicate. Prevents double spending!

### SERVICE PACKAGE (service/)
#### AsyncSettlementService.java
**What it does**: Processes payments asynchronously from Kafka.

**Why it exists**: Separates packet receipt from payment processing.

**When it's used**: When Kafka consumer receives payment event.

**Who calls it**: PaymentEventConsumer or RetryConsumer.

**What it does**:

Takes PaymentEvent from Kafka

Decrypts it using HybridCryptoService

Calls SettlementService to transfer money

If any error, throws exception (Kafka will retry)

**Real-world analogy**: Bank back-office worker processing payments from a queue. Takes one envelope, opens it, sends to settlement department.

#### BridgeIngestionService.java
**What it does**: Handles packets arriving from bridge nodes.

**Why it exists**: First point of contact for mesh packets. Performs all validation before sending to Kafka.

**When it's used**: When bridge node uploads packet.

**Who calls it**: MeshController.flush() or directly from bridge.

**What it does (step by step)**:

Calculate packet hash (SHA-256 of ciphertext)

Check Redis for duplicate (idempotency)

If duplicate → reject immediately

Try to decrypt packet with server private key

If decryption fails → reject (invalid packet)

Check packet age (replay protection)

If packet too old (>24 hours) → reject

Create PaymentEvent

Publish to Kafka topic "payment-ingestion"

Return success

**Real-world analogy**: Mailroom receiving letters from couriers. Checks: Already received? Opened correctly? Too old? Then forwards to processing department.

**Why multiple checks**: Defense in depth. Even if one check fails, others catch duplicates.

#### SettlementService.java
**What it does**: Core payment settlement logic - transfers money between accounts.

**Why it exists**: Actually moves the money.

**When it's used**: After packet is decrypted and validated.

**Who calls it**: AsyncSettlementService.

**What it does (step by step)**:

Find sender account in database (by VPA)

Find receiver account in database

Validate amount is positive

Check if sender has enough balance

If insufficient balance → record REJECTED transaction

Deduct amount from sender (balance = balance - amount)

Add amount to receiver (balance = balance + amount)

Save both accounts to database

Create SETTLED transaction record

Save transaction to database

Increment success metric

Return transaction

**Real-world analogy**: Bank teller transferring money between accounts. Step 1: Find Alice's account. Step 2: Find Bob's account. Step 3: Deduct $100 from Alice. Step 4: Add $100 to Bob. Step 5: Record transaction.

What @Transactional does: All database operations succeed together or all fail together. If deduct works but add fails, deduct is rolled back. No partial updates!

#### MeshSimulatorService.java
**What it does**: Simulates a mesh network of phones/devices.

**Why it exists**: Test offline payment propagation without real phones.

**When it's used**: When demo/testing endpoints are called.

**Who calls it**: DemoController, MeshController.

**What it does**:

Creates virtual devices (phones) with internet or offline status

Devices can hold multiple packets

Gossip: devices share packets with each other

TTL decreases each hop

Bridge devices (with internet) can upload to backend

**Real-world analogy**: A computer simulation of people passing notes in a crowd.

**The default devices are**:

phone-alice (offline) - Alice's phone

phone-stranger1 (offline) - Stranger's phone

phone-stranger2 (offline) - Another stranger

phone-stranger3 (offline) - Another stranger

phone-bridge (has internet) - Bridge node

#### IdempotencyService.java
**What it does**: Prevents duplicate payment processing using Redis.

**Why it exists**: Same packet might arrive multiple times (mesh networks flood). We must process only once.

**When it's used**: Before processing any packet.

**Who calls it**: BridgeIngestionService.

**What it does**:

Tries to set Redis key = packetHash with value "processed"

TTL (Time To Live) = 24 hours (configurable)

setIfAbsent succeeds only if key doesn't exist

Returns true if first time, false if duplicate

**Real-world analogy**: A notebook where you write down which tasks are already done. Before doing a task, check notebook. If not there, do it and write it down. If already written, skip.

**Why Redis instead of database**: Speed! Redis is in-memory (microseconds), database is disk-based (milliseconds).

#### MetricsService.java
**What it does**: Tracks system metrics/counters.

**Why it exists**: Monitor system health and performance.

**When it's used**: Every time a payment succeeds, fails, or retries.

**Who calls it**: SettlementService, RetryConsumer.

What it tracks:

payment_success_total - Successful payments

payment_failed_total - Failed payments

retry_total - Retry attempts

dlq_total - Dead letter queue entries

mesh_packets_total - Packets injected

**Real-world analogy**: A counter clicker tracking how many customers served. Click +1 for success, click +1 for failure.

### KAFKA PACKAGE (kafka/)
#### PaymentEventConsumer.java
**What it does**: Reads payments from Kafka and processes them.

**Why it exists**: Decouples receipt from processing. Bridge can return immediately while processing happens later.

**When it's used**: When Kafka has messages in payment-ingestion topic.

**Who calls it**: Kafka broker automatically.

**What it does**:

Listens to "payment-ingestion" topic

Gets PaymentEvent from Kafka

Calls AsyncSettlementService.process()

If success → commit offset (message removed)

If failure → send to retry topic

**Real-world analogy**: Assembly line worker picking boxes from conveyor belt. Takes box, processes it. If fails, puts on "retry" conveyor.

#### RetryConsumer.java
**What it does**: Retries failed payments.

**Why it exists**: Temporary failures (database down, network issue) need second chance.

**When it's used**: When messages appear in payment-retry topic.

**Who calls it**: Kafka broker automatically.

**What it does**:

Listens to "payment-retry" topic

Tries to process payment again

If success → done

If fails again → send to Dead Letter Queue

Increment DLQ metric

**Real-world analogy**: "Try again later" pile at a post office. Letters that couldn't be delivered get another attempt. If still fails, goes to "dead letter" office.

#### PaymentEventProducer.java
**What it does**: Sends payment events to Kafka.

**Why it exists**: Puts validated packets into the processing queue.

**When it's used**: After BridgeIngestionService validates a packet.

**Who calls it**: BridgeIngestionService.

**What it does**:

Takes PaymentEvent

Sends to "payment-ingestion" topic

Key = packetHash (for consistent partitioning)

Value = PaymentEvent

**Real-world analogy**: Putting a letter into the "outgoing mail" box.

#### RetryProducer.java
**What it does**: Sends failed payments to retry or dead letter topics.

**Why it exists**: Handles failures gracefully.

**When it's used**: When payment processing fails.

**Who calls it**: PaymentEventConsumer or RetryConsumer.

**What it does**:

sendToRetry(): sends to "payment-retry" topic for another attempt

sendToDeadLetter(): sends to "payment-dead-letter" topic for manual review

**Real-world analogy**: Mail clerk deciding: "This needs redelivery" (retry) vs "This cannot be delivered" (dead letter).

### REPOSITORY PACKAGE (repository/)
#### AccountRepository.java
**What it does**: Database operations for Account entities.

**Why it exists**: CRUD operations without writing SQL.

**When it's used**: Whenever we need account data.

**Who calls it**: SettlementService, AccountController.

What it provides: save(), findById(), findAll(), deleteById(), count(), existsById() - all automatically generated!

**Real-world analogy**: A librarian who knows exactly where every book is. "Find account 'alice@demo'" - librarian brings it.

Why interface instead of class: Spring Data JPA automatically creates the implementation at runtime. We just declare the methods we need.

#### TransactionRepository.java
**What it does**: Database operations for Transaction entities.

**Why it exists**: Save transactions, check duplicates, get recent ones.

**When it's used**: When SettlementService processes payments.

**Who calls it**: SettlementService.

**Custom methods**:

findTop20ByOrderByIdDesc(): Get last 20 transactions

existsByPacketHash(): Check if packet already processed

**Real-world analogy**: Ledger keeper who records every transaction and checks if a transaction was already recorded.

**How method naming works**:

findTop20 = LIMIT 20

ByOrderByIdDesc = ORDER BY id DESC

Spring generates SQL automatically!

## PART 5: ALL ANNOTATIONS EXPLAINED
### @SpringBootApplication
**What it does**: Marks the main class of a Spring Boot application.

Why it's used: Combines three annotations into one: @Configuration, @EnableAutoConfiguration, @ComponentScan.

**How Spring processes it**: When you run the application, Spring scans for this annotation and starts:

Embedded web server (Tomcat)

Auto-configuration based on dependencies

Component scanning to find all services, controllers, repositories

**Benefits**: One annotation does everything. No manual configuration needed.

**Alternatives**: Could use @Configuration, @EnableAutoConfiguration, @ComponentScan separately.

### @RestController
**What it does**: Marks a class as a REST API controller.

Why it's used: Handles HTTP requests and automatically converts Java objects to JSON.

**How Spring processes it**:

Scans for @RequestMapping methods

Routes HTTP requests to appropriate methods

Uses Jackson library to convert return values to JSON

Sets Content-Type: application/json header

**Benefits**: No need to manually write JSON conversion code.

**Alternatives**: @Controller (returns HTML pages, not JSON).

### @Service
**What it does**: Marks a class as a service containing business logic.

Why it's used: Spring manages it as a singleton bean (one instance for the whole app).

**How Spring processes it**:

Scans for @Service annotation

Creates one instance (singleton)

Injects dependencies marked with @Autowired

Makes it available for other classes to use

**Benefits**: Separates business logic from web layer. Easy to test.

### @Repository
**What it does**: Marks a class/interface as a Data Access Object.

Why it's used: Spring handles database exceptions and provides JPA features.

**How Spring processes it**:

Creates proxy that implements the interface

Translates database exceptions to Spring's DataAccessException

Enables JPA features like @Entity scanning

**Benefits**: No need to write SQL manually. Spring generates it from method names.

### @Entity
**What it does**: Marks a Java class as a database table.

Why it's used: JPA (Hibernate) maps objects to database tables.

**How Spring processes it**:

Reads class fields

Creates database table if not exists (with ddl-auto=update)

Maps fields to columns

Handles converting between Java types and SQL types

**Benefits**: Work with Java objects, not SQL queries.

### @Autowired
**What it does**: Tells Spring to automatically inject a dependency.

Why it's used: Instead of using "new" to create objects, Spring gives them to you.

**How Spring processes it**:

Scans for @Autowired fields/constructors/setters

Finds matching bean in application context

Injects (assigns) the bean

If no matching bean, throws exception

**Benefits**:

Loose coupling (classes don't create their dependencies)

Easy to test (can inject mock objects)

Spring manages object lifecycle

**Alternatives**: Constructor injection (recommended), setter injection.

### @Transactional
**What it does**: Ensures database operations are atomic (all or nothing).

Why it's used: For money transfers, you cannot have partial updates.

**How Spring processes it**:

Creates a proxy around the method

Starts database transaction before method

Commits transaction if method succeeds

Rolls back transaction if exception occurs

**Example**: If deduct money from sender works but add to receiver fails, entire operation is undone.

**Benefits**: No manual transaction management code needed.

### @KafkaListener
**What it does**: Marks a method to listen to Kafka messages.

Why it's used: To consume messages from Kafka topics asynchronously.

**How Spring processes it**:

Scans for @KafkaListener annotations

Creates Kafka consumer threads

Automatically deserializes messages to Java objects

Calls method when messages arrive

**Benefits**: No manual Kafka consumer code needed.

### @PostConstruct
**What it does**: Marks a method to run after object is created but before it's used.

Why it's used: For initialization code (like generating keys, seeding data).

**How Spring processes it**:

Creates the bean (calls constructor)

Injects dependencies (@Autowired)

Calls @PostConstruct method

Bean is ready to use

**Benefits**: Guaranteed to run after dependencies are injected.

### @GetMapping / @PostMapping
**What it does**: Maps HTTP GET or POST requests to controller methods.

Why it's used: To define REST API endpoints.

**How Spring processes it**:

Reads the URL pattern

When HTTP request matches, calls the method

Converts method parameters from request (query params, body, etc.)

**Benefits**: Declarative routing. No manual servlet code.

### @RequestBody
**What it does**: Tells Spring to convert HTTP request body to Java object.

Why it's used: To receive JSON data from frontend.

**How Spring processes it**:

Reads HTTP request body

Uses Jackson library to parse JSON

Creates Java object and populates fields

Injects as method parameter

**Benefits**: Automatic JSON to object conversion.

### @CrossOrigin
**What it does**: Allows requests from different origins (CORS).

Why it's used: Browsers block cross-origin requests for security. This tells browser "it's OK".

**How Spring processes it**:

Adds CORS headers to HTTP response

Headers include: Access-Control-Allow-Origin, etc.

**Benefits**: Frontend on different port can call backend API.

### @Version
**What it does**: Enables optimistic locking for database updates.

Why it's used: Prevents lost updates when multiple users update same record.

How JPA processes it:

Reads version number when loading entity

When updating, checks version hasn't changed

If changed, throws OptimisticLockException

Automatically increments version on successful update

**Benefits**: No manual locking code needed.

## PART 6: DATABASE DESIGN
## Tables Overview
The database has two main tables: accounts and transactions.

## Accounts Table
This table stores bank account information.

Column	Type	Constraint	Description
vpa	VARCHAR	PRIMARY KEY	Virtual Payment Address (e.g., "alice@demo")
holder_name	VARCHAR	NOT NULL	Full name of account holder
balance	DECIMAL(19,2)	NOT NULL	Current balance (2 decimal places for cents)
version	BIGINT	NULL	Optimistic lock version number
**Why vpa is primary key**: Each account is uniquely identified by its VPA (like email address). No two accounts can have same VPA.

**Why balance uses DECIMAL**:(19,2):

DECIMAL stores exact values (no floating point errors)

19 total digits maximum (big enough for any amount)

2 digits after decimal point (for cents/paise)

**Why version column**: Enables optimistic locking. Prevents two transactions from updating same account simultaneously.

## Transactions Table
This table records every payment attempt (successful or rejected).

Column	Type	Constraint	Description
id	BIGINT	PRIMARY KEY, AUTO_INCREMENT	Unique transaction ID
packet_hash	VARCHAR(64)	NOT NULL, UNIQUE	SHA-256 hash of encrypted packet
sender_vpa	VARCHAR	NOT NULL	Who sent the money
receiver_vpa	VARCHAR	NOT NULL	Who received the money
amount	DECIMAL(19,2)	NOT NULL	Transaction amount
signed_at	TIMESTAMP	NOT NULL	When user created the payment
settled_at	TIMESTAMP	NOT NULL	When server processed it
bridge_node_id	VARCHAR	NOT NULL	Which bridge node delivered
hop_count	INT	NOT NULL	How many mesh hops
status	VARCHAR	NOT NULL	'SETTLED' or 'REJECTED'
**Why packet_hash is UNIQUE**: Prevents double spending. Even if same packet arrives twice, database rejects the duplicate.

**Why both signed_at and settled_at**:

signed_at: when user created payment (from their phone)

settled_at: when server processed it (different times)

**Why status as STRING**: not NUMBER: 'SETTLED' and 'REJECTED' are readable. Number (0,1) would require memorization.

## Indexes
One index is defined:

idx_packet_hash on packet_hash column (UNIQUE)

**Why index**: Makes checking for duplicates super fast. Without index, database would scan entire table.

## Sample Data (Seeded at Startup) (Seeded at Startup)
vpa	holder_name	balance
kartik@demo	Kartik	5000.00
pranay@demo	pranay	1000.00
tanmay@demo	tanmay	2500.00
devid@demo	devid	500.00
These are test accounts created automatically when application starts for the first time.

## PART 7: API DOCUMENTATION
GET /api/accounts
**Purpose**: Get all bank accounts

**Request**: None (no parameters)

**Response**: JSON array of account objects

json
[
{
"vpa": "kartik@demo",
"holderName": "Kartik",
"balance": 5000.00,
"version": 1
}
]
**Status Codes**:

200 OK: Success

POST /api/demo/send
**Purpose**: Send money through the system (demo)

**Request Body**: (JSON):

json
{
"senderVpa": "kartik@demo",
"receiverVpa": "pranay@demo",
"amount": 100.00,
"pin": "1234"
}
**Response**: "Packet injected and sent to Kafka successfully"

**Status Codes**:

200 OK: Success

400 Bad Request: Invalid input

500 Internal Error: Something went wrong

GET /api/mesh/state
**Purpose**: Get current state of all mesh devices

**Request**: None

**Response**: JSON object showing each device and packet count

json
{
"phone-alice": 3,
"phone-stranger1": 2,
"phone-stranger2": 1,
"phone-stranger3": 0,
"phone-bridge": 5
}
**Status Codes**:

200 OK: Success

POST /api/mesh/gossip
**Purpose**: Run one round of packet sharing between devices

**Request**: None

**Response**: JSON with gossip result

json
{
"transfers": 42,
"deviceCounts": {
"phone-alice": 5,
"phone-bridge": 8
}
}
**Status Codes**:

200 OK: Success

POST /api/mesh/flush
**Purpose**: Upload all packets from bridge nodes to backend

**Request**: None

**Response**: "bridge upload complete"

**Status Codes**:

200 OK: Success

POST /api/mesh/reset
**Purpose**: Clear all packets from mesh (start fresh)

**Request**: None

**Response**: "mesh reset"

**Status Codes**:

200 OK: Success

GET /api/transactions
**Purpose**: Get last 20 transactions

**Request**: None

**Response**: JSON array of transaction objects (newest first)

json
[
{
"id": 100,
"packetHash": "a3f5c9e2d1b4...",
"senderVpa": "kartik@demo",
"receiverVpa": "pranay@demo",
"amount": 100.00,
"status": "SETTLED",
"settledAt": "2024-01-15T10:30:00Z"
}
]
**Status Codes**:

200 OK: Success

GET /
**Purpose**: Open dashboard home page

**Request**: None

**Response**: HTML page (dashboard)

**Status Codes**:

200 OK: Success

## PART 8: COMPLETE REQUEST FLOW
## Complete Payment Flow (Step by Step)
**Step  User Creates Payment (Frontend)
User fills out form on webpage or mobile app with:

Sender VPA (who is sending)

Receiver VPA (who is receiving)

Amount (how much)

PIN (for verification)

Frontend sends HTTP POST request to /api/demo/send with JSON body.

**Step  Controller Receives Request
DemoController receives the request. @RequestBody converts JSON to SendMoneyRequest Java object.

**Step  Create Packet (DemoService)
DemoService.createPacket() is called. It:

Creates PaymentInstruction object with sender, receiver, amount, PIN hash, nonce, timestamp

Calls HybridCryptoService.encrypt() to encrypt the instruction

Creates MeshPacket with encrypted ciphertext and metadata

Returns the MeshPacket

**Step  Encryption (HybridCryptoService)
HybridCryptoService.encrypt():

Converts PaymentInstruction to JSON bytes

Generates random AES key (256-bit)

Generates random IV

Encrypts payment data with AES-GCM (fast, any size)

Encrypts AES key with server's RSA public key (secure)

Packages: [encrypted AES key] + [IV] + [AES ciphertext]

Converts to Base64 string

Returns ciphertext

**Step  Inject into Mesh (MeshSimulatorService)
MeshSimulatorService.inject() is called with "phone-alice" as starting device. The packet is added to Alice's virtual device.

**Step  Gossip Through Mesh (MeshSimulatorService)
When /api/mesh/gossip is called:

Each device shares its packets with all other devices

TTL decreases by 1 each hop

Packets with TTL=0 stop forwarding

Duplicate packets are not stored (checked by packetId)

The packet spreads through the mesh: Alice → Stranger1 → Stranger2 → Stranger3 → Bridge

**Step  Upload to Backend (Bridge Node)
Phone-bridge has internet. When /api/mesh/flush is called:

Collect all packets from devices with internet

Call BridgeIngestionService.ingest() for each packet

**Step  Validation (BridgeIngestionService)
BridgeIngestionService.ingest():

Calculates packet hash (SHA-256 of ciphertext)

Checks Redis for duplicate (IdempotencyService.claim())

If duplicate → reject immediately

Decrypts packet (HybridCryptoService.decrypt())

If decryption fails → reject (invalid packet)

Checks packet age (current time - signedAt)

If packet too old (>24 hours) → reject

Creates PaymentEvent

Publishes to Kafka topic "payment-ingestion"

**Step  Kafka Producer (PaymentEventProducer)
PaymentEventProducer.publish():

Sends PaymentEvent to "payment-ingestion" topic

Key = packetHash (ensures same packet always goes to same partition)

Value = PaymentEvent

**Step  Kafka Consumer (PaymentEventConsumer)
### @KafkaListener on PaymentEventConsumer.consume():

Gets triggered when message appears in topic

Extracts PaymentEvent from Kafka

Calls AsyncSettlementService.process()

**Step  Async Settlement (AsyncSettlementService)
AsyncSettlementService.process():

Decrypts packet to get PaymentInstruction

Calls SettlementService.settle()

**Step  Actual Settlement (SettlementService)
SettlementService.settle() (with @Transactional):

Finds sender account in database (AccountRepository.findById)

Finds receiver account in database

Validates amount is positive

Checks if sender has sufficient balance

If insufficient balance → records REJECTED transaction

Deducts amount from sender (balance = balance - amount)

Adds amount to receiver (balance = balance + amount)

Saves both accounts (AccountRepository.save)

Creates SETTLED transaction record

Saves transaction (TransactionRepository.save)

Increments success metric (MetricsService.incrementSuccess)

Returns transaction

**Step  Transaction Recorded
Transaction is saved to PostgreSQL database with status SETTLED.

**Step  Dashboard Updates
Frontend calls GET /api/transactions to see the new transaction in the list.

### Complete Flow Diagram (Text)
text
User → DemoController → DemoService → HybridCryptoService (Encrypt)
↓
MeshSimulatorService (Inject)
↓
[Gossip through Mesh: Alice → Stranger1 → Stranger2 → Bridge]
↓
BridgeIngestionService (Validate + Check duplicate)
↓
PaymentEventProducer (Send to Kafka)
↓
Kafka Topic: payment-ingestion
↓
PaymentEventConsumer (Read from Kafka)
↓
AsyncSettlementService (Decrypt)
↓
SettlementService (Transfer money)
↓
PostgreSQL (Save transaction)
↓
Response back to user
## PART 9: ARCHITECTURE DIAGRAMS (ASCII)
### Overall System Architecture
text
┌─────────────────────────────────────────────────────────────────────────────┐
│                           OFFUPI ARCHITECTURE                               │
│                                                                             │
│  ┌─────────────────┐                                                        │
│  │    Browser      │                                                        │
│  │   (Port 5500)   │                                                        │
│  └────────┬────────┘                                                        │
│           │ HTTP                                                           │
│           ▼                                                                │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    SPRING BOOT APPLICATION (Port 8080)               │   │
│  │                                                                      │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │   │
│  │  │ Controller  │→ │   Service   │→ │ Repository  │                 │   │
│  │  │   Layer     │  │    Layer    │  │   Layer     │                 │   │
│  │  └─────────────┘  └──────┬──────┘  └──────┬──────┘                 │   │
│  │                          │                │                         │   │
│  │                          ▼                ▼                         │   │
│  │                   ┌─────────────┐  ┌─────────────┐                 │   │
│  │                   │   Redis     │  │ PostgreSQL  │                 │   │
│  │                   │   Cache     │  │  Database   │                 │   │
│  │                   └─────────────┘  └─────────────┘                 │   │
│  │                                                                      │   │
│  │  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐   │   │
│  │  │  Producer   │────────→│    Kafka    │────────→│  Consumer   │   │   │
│  │  │             │         │   Topics    │         │             │   │   │
│  │  └─────────────┘         └─────────────┘         └─────────────┘   │   │
│  │                                                                      │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌─────────────┐         ┌─────────────┐         ┌─────────────┐          │
│  │ Prometheus  │────────→│   Grafana   │         │   Docker    │          │
│  │  (Metrics)  │         │ (Dashboard) │         │  Container  │          │
│  └─────────────┘         └─────────────┘         └─────────────┘          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
### Mesh Network Simulation
text
┌─────────────────────────────────────────────────────────────────────────────┐
│                         MESH NETWORK SIMULATION                             │
│                                                                             │
│                          ┌─────────────────┐                               │
│                    ┌────→│  phone-alice    │                               │
│                    │     │   (Offline)     │                               │
│                    │     └────────┬────────┘                               │
│                    │              │                                         │
│                    │              │ Gossip                                  │
│                    │              ▼                                         │
│  ┌─────────────────┼──┐     ┌─────────────┐                               │
│  │                 │  │     │ phone-      │                               │
│  │    Packet       │  └────→│ stranger1   │                               │
│  │    Travels      │        │ (Offline)   │                               │
│  │    Through      │        └──────┬──────┘                               │
│  │    Devices      │               │                                       │
│  │                 │               │ Gossip                                │
│  │                 │               ▼                                       │
│  │                 │        ┌─────────────┐                               │
│  │                 │        │ phone-      │                               │
│  │                 │        │ stranger2   │                               │
│  │                 │        │ (Offline)   │                               │
│  │                 │        └──────┬──────┘                               │
│  │                 │               │                                       │
│  │                 │               │ Gossip                                │
│  │                 │               ▼                                       │
│  │                 │        ┌─────────────┐                               │
│  │                 └───────→│ phone-      │                               │
│  │                          │ stranger3   │                               │
│  │                          │ (Offline)   │                               │
│  │                          └──────┬──────┘                               │
│  │                                 │                                       │
│  │                                 │ Gossip                                │
│  │                                 ▼                                       │
│  │                          ┌─────────────┐                               │
│  │                          │ phone-      │                               │
│  │                          │ bridge      │                               │
│  │                          │ (Internet)  │                               │
│  │                          └──────┬──────┘                               │
│  │                                 │                                       │
│  │                                 │ Upload to Backend                     │
│  │                                 ▼                                       │
│  │                          ┌─────────────┐                               │
│  │                          │   Backend   │                               │
│  │                          │   Server    │                               │
│  │                          └─────────────┘                               │
│  └─────────────────────────────────────────────────────────────────────────┘
### Database Schema
text
┌─────────────────────────────────────────────────────────────────────────────┐
│                            DATABASE SCHEMA                                  │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         accounts TABLE                                │   │
│  ├─────────────┬─────────────────────────────┬─────────────────────────┤   │
│  │ vpa (PK)    │ holder_name                 │ balance                 │   │
│  ├─────────────┼─────────────────────────────┼─────────────────────────┤   │
│  │ VARCHAR     │ VARCHAR                     │ DECIMAL(19,2)           │   │
│  │ "alice@demo"│ "Alice"                     │ 5000.00                 │   │
│  │ "bob@demo"  │ "Bob"                       │ 1000.00                 │   │
│  └─────────────┴─────────────────────────────┴─────────────────────────┘   │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                       transactions TABLE                             │   │
│  ├─────────┬───────────────┬─────────────┬─────────────┬───────────────┤   │
│  │ id (PK) │ packet_hash    │ sender_vpa  │ receiver_vpa│ amount        │   │
│  ├─────────┼───────────────┼─────────────┼─────────────┼───────────────┤   │
│  │ BIGINT  │ VARCHAR(64)    │ VARCHAR     │ VARCHAR     │ DECIMAL(19,2) │   │
│  │ 1       │ "a3f5c9e2..."  │ "alice@demo"│ "bob@demo"  │ 100.00        │   │
│  │ 2       │ "b4g6d0f3..."  │ "bob@demo"  │ "carol@demo"│ 50.00        │   │
│  └─────────┴───────────────┴─────────────┴─────────────┴───────────────┘   │
│                                    │                                        │
│                                    │ UNIQUE INDEX                          │
│                                    ▼                                        │
│                          ┌─────────────────────┐                           │
│                          │ idx_packet_hash     │                           │
│                          │ (UNIQUE)            │                           │
│                          └─────────────────────┘                           │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                           Redis Cache                                │   │
│  │                                                                       │   │
│  │  Key                    Value        TTL                              │   │
│  │  ─────────────────────────────────────────────────────────────────   │   │
│  │  "a3f5c9e2d1b4..."      "processed"  86400 seconds (24 hours)        │   │
│  │  "b4g6d0f3e2c5..."      "processed"  86400 seconds                    │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
### Kafka Topic Flow
text
┌─────────────────────────────────────────────────────────────────────────────┐
│                           KAFKA TOPIC FLOW                                  │
│                                                                             │
│  ┌─────────────────┐                                                        │
│  │ BridgeIngestion │                                                        │
│  │    Service      │                                                        │
│  └────────┬────────┘                                                        │
│           │                                                                 │
│           │ Validated Packet                                                │
│           ▼                                                                 │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    TOPIC: payment-ingestion                          │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │   │
│  │  │ Partition 0 │  │ Partition 1 │  │ Partition 2 │                 │   │
│  │  │ Msg 1       │  │ Msg 2       │  │ Msg 3       │                 │   │
│  │  │ Msg 4       │  │ Msg 5       │  │ Msg 6       │                 │   │
│  │  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘                 │   │
│  └─────────┼────────────────┼────────────────┼────────────────────────┘   │
│            │                │                │                             │
│            ▼                ▼                ▼                             │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐                    │
│  │ Consumer 1  │    │ Consumer 2  │    │ Consumer 3  │                    │
│  │ (Processes  │    │ (Processes  │    │ (Processes  │                    │
│  │ Partition 0)│    │ Partition 1)│    │ Partition 2)│                    │
│  └──────┬──────┘    └──────┬──────┘    └──────┬──────┘                    │
│         │                  │                  │                           │
│         └──────────────────┼──────────────────┘                           │
│                            │                                              │
│                      If Failure                                           │
│                            ▼                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                      TOPIC: payment-retry                            │   │
│  │                           (for retry)                                │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                            │                                              │
│                      If Fails Again                                       │
│                            ▼                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                   TOPIC: payment-dead-letter                         │   │
│  │                    (needs manual review)                             │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
### Deployment Architecture (Docker)
text
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DOCKER DEPLOYMENT                                    │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                         HOST MACHINE                                 │   │
│  │                                                                      │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │   │
│  │  │ PostgreSQL   │  │    Redis     │  │   Zookeeper  │              │   │
│  │  │   :5432      │  │   :6379      │  │   :2181      │              │   │
│  │  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘              │   │
│  │         │                 │                 │                       │   │
│  │         ▼                 ▼                 ▼                       │   │
│  │  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐              │   │
│  │  │    Kafka     │  │     App      │  │  Prometheus  │              │   │
│  │  │   :9092      │  │   :8080      │  │   :9090      │              │   │
│  │  └──────────────┘  └──────────────┘  └──────┬───────┘              │   │
│  │                                              │                       │   │
│  │                                              ▼                       │   │
│  │                                       ┌──────────────┐              │   │
│  │                                       │   Grafana    │              │   │
│  │                                       │   :3000      │              │   │
│  │                                       └──────────────┘              │   │
│  │                                                                      │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  Port Mappings:                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │ Container Port : Host Port  │ Access                                 │   │
│  │ 5432 (PG)      : 5433       │ localhost:5433                         │   │
│  │ 6379 (Redis)   : 6380       │ localhost:6380                         │   │
│  │ 9092 (Kafka)   : 9093       │ localhost:9093                         │   │
│  │ 8080 (App)     : 8081       │ http://localhost:8081                  │   │
│  │ 9090 (Prom)    : 9091       │ http://localhost:9091                  │   │
│  │ 3000 (Grafana) : 3001       │ http://localhost:3001                  │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
## PART 10: DESIGN PATTERNS USED
1. Dependency Injection Pattern
   What it is: Instead of creating dependencies with "new", Spring gives them to you.

Where it's used: Every @Autowired field or constructor parameter.

**Example**: SettlementService doesn't create AccountRepository. Spring injects it.

Why it's good:

Loose coupling (classes don't know how dependencies are created)

Easy to test (can inject mock objects)

Spring manages object lifecycle

2. Repository Pattern
   What it is: Abstracts database operations behind an interface.

Where it's used: AccountRepository, TransactionRepository.

**Example**: accountRepository.findById() - you don't care if it's SQL, NoSQL, or file.

Why it's good:

Can change database without changing business code

Centralizes database logic

Easy to mock for testing

3. DTO Pattern (Data Transfer Object)
   What it is: Separate object for API data, different from database entity.

Where it's used: SendMoneyRequest.

Why it's good:

Don't expose database entities to clients

Can combine data from multiple entities

Can hide sensitive fields (like password hash)

4. Singleton Pattern
   What it is: Only one instance of a class exists in the entire application.

Where it's used: All @Service, @Repository, @Controller classes.

Why it's good:

Saves memory (one instance vs many)

Consistent state across application

Spring manages it automatically

5. Factory Pattern
   What it is: Method that creates objects instead of using "new" directly.

Where it's used: TopicBuilder in KafkaConfig, IngestResult static methods.

**Example**: TopicBuilder.name().partitions().replicas().build()

Why it's good:

Centralizes object creation logic

Can create complex objects step by step

Easier to change creation logic

6. Observer Pattern (Event Listener)
   What it is: Objects listen for events and react when they happen.

Where it's used: @KafkaListener for Kafka messages.

**Example**: PaymentEventConsumer "observes" the Kafka topic.

Why it's good:

Decouples event producer from consumer

Multiple listeners can react to same event

Asynchronous processing

7. Template Method Pattern
   What it is: Framework provides skeleton, you fill in the details.

Where it's used: JpaRepository interface.

**Example**: You declare methods like "findTop20ByOrderByIdDesc", Spring implements SQL.

Why it's good:

Less boilerplate code

Consistent patterns

Framework handles complex parts

8. Proxy Pattern
   What it is: Framework wraps your object to add behavior.

Where it's used: @Transactional, @KafkaListener.

**Example**: Spring creates proxy around @Transactional method to start/commit transactions.

Why it's good:

Adds cross-cutting concerns (transaction, logging, security)

You don't write repetitive code

Framework handles it automatically

## PART 11: INTERVIEW PREPARATION
Explain the Project to an Interviewer (1 Minute)
"OFFUPI is an offline payment system I built using Java and Spring Boot. It allows people to send money without internet by using mesh networking - phones relay encrypted messages to each other like a chain. When a message finally reaches a phone with internet, it gets uploaded to the backend, processed, and the money is transferred. The system uses hybrid encryption (RSA + AES) for security, Kafka for async processing, Redis for preventing duplicates, and Docker for easy deployment."

Why Did You Choose Each Technology?
Technology	Why Chosen
Java 21	LTS version, virtual threads, enterprise standard
Spring Boot	Auto-configuration, huge ecosystem, production-ready
PostgreSQL	ACID compliant, reliable for money, free
Redis	In-memory = super fast for duplicate checks
Kafka	High throughput, persistent storage, replayable
RSA + AES	Hybrid encryption = fast + secure (what HTTPS uses)
Docker	One command to run all services, consistent environment
Architecture Decisions Explained
Decision: Why async processing with Kafka?

Without Kafka: Bridge node would wait for payment processing (slow)

With Kafka: Bridge returns immediately, payment processes later

Handles traffic spikes (Kafka buffers messages)

If consumer crashes, messages aren't lost (Kafka stores them)

Decision: Why Redis for idempotency instead of database?

Redis is in-memory (microseconds) vs database (milliseconds)

Every request checks idempotency first (needs to be fast)

But we also have database UNIQUE constraint as backup

Decision: Why hybrid encryption instead of just RSA?

RSA alone can only encrypt ~245 bytes (too small for our data)

AES alone is fast but needs shared key (how to share safely?)

Hybrid: AES encrypts data (any size, fast), RSA encrypts AES key (secure)

Decision: Why 3 Kafka partitions?

More partitions = more parallel processing

3 allows 3 consumers to run simultaneously

Too many partitions = overhead (more files, more connections)

3 is balanced for this scale

Trade-offs Made
Trade-off	Choice	Why
Consistency vs Availability	Consistency	For money, we can't lose transactions
Speed vs Security	Both (hybrid encryption)	AES is fast, RSA is secure
Simplicity vs Scalability	Scalability	Kafka adds complexity but handles load
Cost vs Performance	Performance	Redis is fast but uses RAM (more expensive)
Beginner Interview Questions
Q1: What is @Autowired?
A: It tells Spring to automatically inject a dependency. Instead of writing "AccountRepository repo = new AccountRepository()", Spring gives you the object. This makes code loosely coupled and easier to test.

Q2: What is @Transactional?
A: It ensures all database operations succeed together or all fail together. For money transfer, if deducting from sender works but adding to receiver fails, the deduct is rolled back. No partial updates!

Q3: What is the difference between @Controller and @RestController?
A: @Controller returns HTML page names. @RestController returns JSON data directly. @RestController = @Controller + @ResponseBody.

Q4: What is a DTO?
A: Data Transfer Object. It carries data between layers. We don't expose database entities directly to API clients. DTOs let us control exactly what data is sent.

Q5: What is the repository pattern?
A: It abstracts database operations behind an interface. You call accountRepository.findById() - you don't care if it's SQL, NoSQL, or a file. You can change database without changing business code.

Intermediate Interview Questions
Q6: How does hybrid encryption work?
A: Step 1: Generate random AES key for this payment only. Step 2: Encrypt payment data with AES (fast, any size). Step 3: Encrypt the AES key with server's RSA public key (secure). Step 4: Send both together. Server decrypts AES key with RSA private key, then decrypts payment with AES key.

Q7: How do you prevent duplicate payments?
A: Three layers: 1) Redis setIfAbsent - first check (fast), 2) Database UNIQUE constraint on packet_hash - backup, 3) Replay protection - packet age check (>24 hours rejected). Defense in depth.

Q8: What is optimistic locking with @Version?
A: Each account has version number. When you read account, you get version=5. When you update, you check version is still 5. If someone else updated first (version now 6), your update fails. Prevents lost updates without locking database rows.

Q9: How does the mesh gossip work?
A: Each device shares all packets with every other device. TTL decreases by 1 each hop. Devices don't store duplicates (checked by packetId). This simulates Bluetooth/WiFi Direct spreading in offline environments.

Q10: What happens if Kafka consumer crashes?
A: Kafka stores messages on disk. Consumer group rebalances - another consumer gets the partitions. Messages are reprocessed from last committed offset. No messages lost!

Advanced Interview Questions
Q11: How would you scale this system?
A:

Horizontal scaling: Run multiple Spring Boot instances behind load balancer

Increase Kafka partitions for more parallel consumers

Database read replicas for transaction queries

Shard accounts table by VPA range

Redis cluster for idempotency (distributed cache)

Q12: How would you handle private key security in production?
A: Never store private key in code or JAR. Use:

HSM (Hardware Security Module) - dedicated hardware

KMS (AWS KMS, HashiCorp Vault) - managed service

Environment variable with encrypted value

Secrets manager (Kubernetes secrets)

Q13: How would you test the mesh network?
A:

Unit tests: VirtualDevice holds packets, TTL decreases

Integration tests: gossipOnce() propagates correctly

Chaos testing: Random device failures, network partitions

Load testing: Many packets through limited devices

Q14: How would you handle message ordering?
A: Use same key (packetHash) for each payment. Kafka guarantees order within a partition. All messages with same key go to same partition. For a specific payment, messages are processed in order.

Q15: How would you implement exactly-once semantics?
A:

Idempotent producer (Kafka 0.11+)

Transactional consumer (read-process-write atomically)

Use packet_hash as unique key in database

Combine with Redis idempotency check

Result: Exactly-once processing for each payment

## PART 12: PROJECT VIVA GUIDE
1 Minute Explanation
"OFFUPI is an offline payment system. It lets you send money without internet by having phones relay encrypted messages to each other. When a message reaches a phone with internet, it's uploaded to our server, decrypted, and the money is transferred. We use Spring Boot for the backend, PostgreSQL for data, Redis to prevent duplicates, Kafka for async processing, and Docker for deployment."

3 Minute Explanation
"OFFUPI solves the problem of sending money without internet access. Traditional payment systems like UPI or credit cards all need internet. Our solution uses mesh networking - phones relay messages to each other like a chain.

Here's how it works step by step:

You create a payment on your phone (offline)

Your phone encrypts it using hybrid encryption (RSA + AES)

Your phone sends it to nearby phones via Bluetooth/WiFi Direct

Phones keep passing it until someone reaches internet

That phone (bridge node) uploads to our server

Server checks for duplicates using Redis

Server decrypts the payment

Server transfers money between accounts

Transaction is recorded in PostgreSQL

For the backend, I used Spring Boot for the web framework, PostgreSQL for reliable data storage, Redis for fast duplicate detection, Kafka for asynchronous payment processing, and Docker to run everything with one command. The system also has retry mechanisms - if processing fails temporarily, we retry. If it fails permanently, it goes to dead letter queue for manual review."

5 Minute Explanation
[Include the 3 minute explanation plus these additional details]

Security: We use hybrid encryption because RSA alone can only encrypt about 245 bytes (too small for our payment data), while AES is fast but needs a shared key. So we encrypt the payment with AES and encrypt the AES key with RSA. This is exactly what HTTPS uses.

Idempotency: To prevent double spending, we have three layers. First, we check Redis cache. If not there, we process. Second, database has unique constraint on packet hash. Third, we check packet age - anything older than 24 hours is rejected.

Mesh Simulation: I built a virtual mesh simulator with 5 devices. Alice's phone starts with the packet. When we run gossip, all devices share packets. TTL (Time To Live) decreases each hop. When packet reaches the bridge node (has internet), we upload to backend.

Async Processing: When packet arrives at bridge, we don't process immediately. We publish to Kafka topic 'payment-ingestion' and return immediately. Kafka consumers read from topic and process payments. This decouples receipt from processing and handles traffic spikes.

Failure Handling: If main consumer fails, we send to 'payment-retry' topic. Retry consumer tries again. If that fails, we send to 'payment-dead-letter' for manual review. We track all failures with Prometheus metrics.

Deployment: Everything runs in Docker containers. One docker-compose up starts PostgreSQL, Redis, Kafka, Zookeeper, Prometheus, Grafana, and the Spring Boot app. This makes deployment consistent across environments.

Detailed Explanation (All Concepts)
[Everything covered in the previous sections - this document is the detailed explanation!]

## PART 13: RESUME SECTION
Short Description (1 line)
Built an offline payment system using Java Spring Boot that allows money transfer without internet through mesh networking and hybrid encryption.

Medium Description (2-3 lines)
Developed OFFUPI, an offline payment system using Spring Boot, Kafka, Redis, and PostgreSQL. Implemented hybrid encryption (RSA + AES) for security, mesh network simulation for offline propagation, and idempotency to prevent double spending. Deployed with Docker and monitored with Prometheus + Grafana.

Detailed Description (5-6 lines)
Built a complete offline payment system that enables money transfer without internet connectivity. The system uses mesh networking where phones relay encrypted messages to each other until reaching a bridge node with internet. Implemented hybrid encryption (RSA-2048 + AES-256) for secure payment data, Apache Kafka for asynchronous processing with retry and dead-letter queues, Redis for idempotency to prevent duplicate payments, and PostgreSQL for persistent storage. Created a simulated mesh network with 5 devices for testing offline packet propagation. Deployed all services (PostgreSQL, Redis, Kafka, Zookeeper, Prometheus, Grafana) using Docker Compose with one command. Included monitoring with Prometheus metrics and Grafana dashboards showing success rates, failure rates, and retry counts.

Key Achievements
✅ Enabled payments in 100% offline environments (no internet required to send)

✅ Achieved near real-time processing through async Kafka pipeline

✅ Prevented double spending with 3-layer idempotency (Redis + DB + time check)

✅ Secured payment data with military-grade hybrid encryption (RSA-2048 + AES-256)

✅ Built complete monitoring stack with 5+ metrics tracked in Grafana

✅ Containerized 7 services with Docker for one-command deployment

Skills Demonstrated
Skill	How Demonstrated
Java 21	Entire backend codebase
Spring Boot	REST APIs, DI, Data JPA, Kafka
PostgreSQL	Database design, transactions, indexes
Redis	Idempotency caching, TTL
Apache Kafka	Async processing, retry topics, DLQ
Security	RSA + AES hybrid encryption
Docker	Multi-container deployment
Monitoring	Prometheus metrics, Grafana dashboards
Architecture	Microservices, event-driven, mesh networking
## PART 14: LESSONS LEARNED & IMPROVEMENTS
What I Learned From This Project
Technical Lessons:

Hybrid encryption is essential when you need both security and performance

Idempotency needs multiple layers (defense in depth) - one layer can fail

Async processing with Kafka decouples components beautifully

Docker Compose makes complex multi-service apps easy to run

Optimistic locking with @Version prevents lost updates without performance hit

Mesh networks need TTL (Time To Live) to prevent infinite loops

Redis is incredibly fast for key-value lookups (microseconds vs milliseconds)

Architecture Lessons:

Separation of concerns (Controller → Service → Repository) makes code maintainable

DTOs should never expose database entities directly

Event-driven architecture is great for systems with varying load

Monitoring must be built in from day one, not added later

Process Lessons:

Start with simple working version, then add complexity

Test each layer independently before integration

Documentation is as important as code

Important Concepts Used
Concept	Where Used	Why Important
ACID Transactions	SettlementService	Money transfers must be atomic
Idempotency	IdempotencyService + DB	Prevents double spending
Hybrid Encryption	HybridCryptoService	Fast + secure
Optimistic Locking	Account.version	Prevents lost updates
TTL (Time To Live)	MeshPacket.ttl	Prevents infinite loops
Event-Driven	Kafka topics	Async processing
Singleton Pattern	All @Service classes	One instance per app
Dependency Injection	@Autowired everywhere	Loose coupling
Industry Best Practices Followed
✅ REST API Design: Proper HTTP methods, meaningful URLs, JSON responses

✅ Package by Feature: Each package has clear responsibility (controller, service, repository)

✅ Constructor Injection: Better than field injection (easier testing, immutable)

✅ DTOs separate from Entities: No direct entity exposure in APIs

✅ Validation: @Valid annotations on DTOs

✅ Logging: SLF4J with appropriate log levels (info, warn, error)

✅ Exception Handling: Consistent error responses

✅ Configuration Externalized: Database credentials in properties, not code

✅ Idempotency Keys: Prevent duplicate processing

✅ Dead Letter Queue: Failed messages stored for manual review

✅ Health Checks: Actuator endpoints for monitoring

✅ Metrics: Prometheus for all important counters

✅ Containerization: Docker for consistent environments

Weak Points in Current Project
No Authentication/Authorization: Anyone can send payments. Needs JWT or OAuth2.

No Rate Limiting: Could be flooded with requests.

No Audit Logs: Can't track who accessed what.

Single Database: No read replicas for query scaling.

No Backup Strategy: Database backups not implemented.

Mesh Simulator Not Real: Real Bluetooth/WiFi Direct would be complex.

Hardcoded TTL: Should be configurable per device type.

No End-to-End Encryption Tests: Need more crypto validation.

Seeded Accounts Only: No user registration.

No CI/CD Pipeline: Manual build and deploy.

Suggested Improvements
Short Term (Easy):

Add API rate limiting (Bucket4j)

Add request ID logging for tracing

Add Swagger/OpenAPI documentation

Add more unit tests

Add health check endpoints

Medium Term (1-2 weeks):

Implement JWT authentication

Add user registration and login

Add audit logging (who accessed what)

Implement database backups

Add integration tests for full flow

Long Term (Month):

Real mobile app with Bluetooth/WiFi Direct

Multiple database replicas for read scaling

Kafka Streams for real-time analytics

Machine learning for fraud detection

Kubernetes deployment for auto-scaling

Production-Ready Improvements
To make this production-ready, I would add:

Security:

OAuth2 + JWT authentication

API rate limiting per user

Secrets management (Vault)

SSL/TLS for all communications

Reliability:

Database read replicas

Kafka replication factor = 3

Redis cluster mode

Health checks with auto-healing

Observability:

Distributed tracing (Jaeger)

Centralized logging (ELK stack)

Alerting (Prometheus AlertManager)

SLI/SLO tracking

Operations:

CI/CD pipeline (GitHub Actions)

Infrastructure as Code (Terraform)

Blue-green deployment

Database migration tool (Flyway)

Professional Architecture Recommendations
For enterprise-scale deployment, I would recommend:

Move to Kubernetes instead of Docker Compose for auto-scaling and self-healing

Service Mesh (Istio) for service-to-service communication, retries, circuit breakers

Event Sourcing for complete audit trail of all payment state changes

CQRS (Command Query Responsibility Segregation) - separate paths for reads and writes

API Gateway (Spring Cloud Gateway) for routing, rate limiting, authentication

Distributed Cache (Redis Cluster) for shared idempotency across instances

Database Sharding by VPA hash for horizontal scaling

Kafka Streams for real-time fraud detection

Chaos Engineering to test failure scenarios

SRE Practices with error budgets and SLIs

END OF DOCUMENT
This document covers everything in the OFFUPI project from beginner to advanced level. Use it as your complete reference guide.

This response is AI-generated, for reference only.
