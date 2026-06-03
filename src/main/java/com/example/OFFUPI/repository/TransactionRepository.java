// SUMMARY: This repository handles database operations for Transaction entities.
// It provides custom methods to find recent transactions and check for duplicates.
// Spring Data JPA parses method names to generate SQL queries automatically.

package com.example.OFFUPI.repository;

import com.example.OFFUPI.entity.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// This interface extends JpaRepository to get standard CRUD operations
// JpaRepository<Transaction, Long> means:
//   - Transaction: The entity type (maps to transactions table)
//   - Long: The data type of the primary key (@Id field, which is 'id')
public interface TransactionRepository
        extends JpaRepository<Transaction, Long> {

    // Custom method: Find the top 20 most recent transactions
    // Spring Data JPA parses the method name and generates SQL automatically
    //
    // Method name breakdown:
    // ┌─────────────┐
    // │ findTop20   │ → LIMIT 20 (only return 20 records)
    // └─────────────┘
    // ┌─────────────────┐
    // │ ByOrderByIdDesc │ → ORDER BY id DESC (newest first, because higher id = newer)
    // └─────────────────┘
    //
    // Generated SQL (approximately):
    // SELECT * FROM transactions ORDER BY id DESC LIMIT 20
    //
    // Used for: Displaying recent payments on a dashboard
    List<Transaction> findTop20ByOrderByIdDesc();

    // Custom method: Check if a transaction with given packet hash already exists
    // Method name breakdown:
    // ┌───────────┐
    // │ exists    │ → Returns boolean (true/false)
    // └───────────┘
    // ┌────────────────┐
    // │ ByPacketHash   │ → WHERE packet_hash = ?
    // └────────────────┘
    //
    // Generated SQL (approximately):
    // SELECT COUNT(*) > 0 FROM transactions WHERE packet_hash = ?
    //
    // Used for: Idempotency check - prevents processing the same packet twice
    // Returns true if packet already processed, false if new
    boolean existsByPacketHash(String packetHash);
}