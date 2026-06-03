// SUMMARY: This controller handles API requests for payment transactions.
// It provides endpoints to view recent transaction history.
// URL pattern: /api/transactions - view payment records.

package com.example.OFFUPI.controller;

import com.example.OFFUPI.entity.Transaction;
import com.example.OFFUPI.repository.TransactionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController - Returns JSON data for transaction API calls
@RestController

// All endpoints start with /api/transactions
@RequestMapping("/api/transactions")
public class TransactionController {

    // Auto-inject the repository that handles database operations for transactions
    @Autowired
    private TransactionRepository repository;

    // This method handles GET requests to /api/transactions
    // Purpose: Get the 20 most recent transactions (for dashboard display)
    @GetMapping
    public List<Transaction> transactions() {

        // repository.findTop20ByOrderByIdDesc() - Custom Spring Data JPA method
        // Translation: "Find the first 20 transactions, sorted by ID from newest to oldest"
        // The method name follows Spring Data JPA naming convention:
        // "findTop20" = limit to 20 results
        // "ByOrderByIdDesc" = sort by id column in descending order (largest id = newest)
        return repository.findTop20ByOrderByIdDesc();
    }
}