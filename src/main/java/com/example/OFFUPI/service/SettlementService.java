// SUMMARY: This service handles the actual money transfer between accounts.
// It validates the payment, deducts from sender, adds to receiver, and records the transaction.
// This is the CORE BUSINESS LOGIC of the entire payment system.

package com.example.OFFUPI.service;

import com.example.OFFUPI.entity.Account;
import com.example.OFFUPI.entity.PaymentInstruction;
import com.example.OFFUPI.entity.Transaction;
import com.example.OFFUPI.repository.AccountRepository;
import com.example.OFFUPI.repository.TransactionRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

// @Service - This service contains the core payment settlement logic
@Service
public class SettlementService {

    // Logger for recording settlement events
    private static final Logger log =
            LoggerFactory.getLogger(SettlementService.class);

    // Repository for account database operations
    @Autowired
    private AccountRepository accounts;

    // Repository for transaction database operations
    @Autowired
    private TransactionRepository transactions;

    // Service for tracking metrics (success/failure counts)
    @Autowired
    private MetricsService metricsService;

    // @Transactional means: "This method runs inside a database transaction"
    // If ANY operation fails, ALL changes are rolled back (no partial updates)
    // Example: If deduct money succeeds but add money fails, deduct is undone
    @Transactional
    public Transaction settle(
            PaymentInstruction instruction,  // Decrypted payment details
            String packetHash,              // Unique hash for idempotency
            String bridgeNodeId,            // Which bridge node delivered
            int hopCount                    // How many hops through mesh
    ) {

        // STEP 1: Find the sender's account in database
        Account sender = accounts.findById(
                instruction.getSenderVpa()   // Look up by VPA (Virtual Payment Address)
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Unknown sender VPA: "
                                + instruction.getSenderVpa()
                )
        );

        // STEP 2: Find the receiver's account
        Account receiver = accounts.findById(
                instruction.getReceiverVpa()
        ).orElseThrow(() ->
                new IllegalArgumentException(
                        "Unknown receiver VPA: "
                                + instruction.getReceiverVpa()
                )
        );

        // STEP 3: Get the amount to transfer
        BigDecimal amount =
                instruction.getAmount();

        // STEP 4: Validate amount is positive (greater than 0)
        // signum() returns: -1 for negative, 0 for zero, 1 for positive
        if (amount.signum() <= 0) {

            // Increment failure counter for monitoring
            metricsService.incrementFailure();

            // Throw exception (transaction will roll back)
            throw new IllegalArgumentException(
                    "Amount must be positive"
            );
        }

        // STEP 5: Check if sender has enough money
        // compareTo() returns: negative if balance < amount, 0 if equal, positive if balance > amount
        if (sender.getBalance().compareTo(amount) < 0) {

            // Log the insufficient balance
            log.warn(
                    "Insufficient balance: {} has ${}, tried to send ${}",
                    sender.getVpa(),
                    sender.getBalance(),
                    amount
            );

            // Increment failure counter
            metricsService.incrementFailure();

            // Record as REJECTED transaction (not successful, but record for audit)
            return recordRejected(
                    instruction,
                    packetHash,
                    bridgeNodeId,
                    hopCount
            );
        }

        // STEP 6: Deduct amount from sender (subtract)
        sender.setBalance(
                sender.getBalance().subtract(amount)  // balance = balance - amount
        );

        // STEP 7: Add amount to receiver (add)
        receiver.setBalance(
                receiver.getBalance().add(amount)     // balance = balance + amount
        );

        // STEP 8: Save both accounts to database
        accounts.save(sender);    // UPDATE accounts SET balance = ? WHERE vpa = ?
        accounts.save(receiver);  // UPDATE accounts SET balance = ? WHERE vpa = ?

        // STEP 9: Create a successful transaction record
        Transaction tx = new Transaction();

        tx.setPacketHash(packetHash);                    // Unique hash for deduplication
        tx.setSenderVpa(instruction.getSenderVpa());    // Who sent
        tx.setReceiverVpa(instruction.getReceiverVpa());// Who received
        tx.setAmount(amount);                            // How much

        // Set when sender originally signed (could be null for old packets)
        tx.setSignedAt(
                instruction.getSignedAt() != null
                        ? Instant.ofEpochMilli(instruction.getSignedAt())
                        : Instant.now()
        );

        tx.setSettledAt(Instant.now());                  // When we processed it
        tx.setBridgeNodeId(bridgeNodeId);                // Which bridge delivered
        tx.setHopCount(hopCount);                        // How many hops
        tx.setStatus(Transaction.Status.SETTLED);        // SETTLED = success

        // STEP 10: Save transaction to database
        transactions.save(tx);

        // STEP 11: Increment success counter
        metricsService.incrementSuccess();

        // STEP 12: Log successful settlement
        log.info(
                "SETTLED ${} from {} to {}",
                amount,
                sender.getVpa(),
                receiver.getVpa()
        );

        // Return the transaction record
        return tx;
    }

    // Helper method: Record a rejected transaction (insufficient funds, invalid account, etc.)
    // Rejected means the payment was attempted but failed permanently
    private Transaction recordRejected(
            PaymentInstruction instruction,
            String packetHash,
            String bridgeNodeId,
            int hopCount
    ) {

        // Create a transaction record with REJECTED status
        Transaction tx = new Transaction();

        tx.setPacketHash(packetHash);
        tx.setSenderVpa(instruction.getSenderVpa());
        tx.setReceiverVpa(instruction.getReceiverVpa());
        tx.setAmount(instruction.getAmount());
        tx.setSignedAt(Instant.ofEpochMilli(instruction.getSignedAt()));
        tx.setSettledAt(Instant.now());
        tx.setBridgeNodeId(bridgeNodeId);
        tx.setHopCount(hopCount);
        tx.setStatus(Transaction.Status.REJECTED);  // REJECTED = permanent failure

        // Save rejected transaction to database
        return transactions.save(tx);
    }
}