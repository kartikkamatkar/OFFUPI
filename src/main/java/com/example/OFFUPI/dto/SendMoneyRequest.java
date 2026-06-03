// SUMMARY: This is a DTO (Data Transfer Object) that carries payment request data from client to server.
// DTOs are simple containers that hold data - they have NO business logic, just fields and getters/setters.
// This specific DTO represents what a client sends when they want to transfer money.
// Example JSON from frontend: {"senderVpa":"alice@bank","receiverVpa":"bob@bank","amount":100.50,"pin":"1234"}

package com.example.OFFUPI.dto;

// Import BigDecimal for precise money calculations (never use double/float for money!)
// BigDecimal handles decimal numbers without rounding errors (0.1 + 0.2 = 0.3 exactly)
import java.math.BigDecimal;

// This class is a DTO (Data Transfer Object)
// DTOs are used to transfer data between different parts of the application
// Here: Frontend (browser) → Controller (backend)
public class SendMoneyRequest {

    // Sender's VPA (Virtual Payment Address) - like an email address for money
    // Example: "alice@icici" or "john@sbi"
    private String senderVpa;

    // Receiver's VPA - who gets the money
    // Example: "bob@hdfc" or "store@paytm"
    private String receiverVpa;

    // Amount to send - using BigDecimal for accuracy with money
    // BigDecimal is safe because 0.01 + 0.02 = 0.03 exactly (no floating point errors)
    private BigDecimal amount;

    // PIN (Personal Identification Number) - password to authorize the transaction
    // In real apps, this would be hashed, not stored as plain text
    private String pin;

    // No-argument constructor (required by frameworks like Jackson for JSON conversion)
    // When Spring receives JSON, it creates an empty object first, then fills fields
    public SendMoneyRequest() {
    }

    // Getter for senderVpa - allows other code to READ the sender's VPA
    // Example: request.getSenderVpa() returns "alice@icici"
    public String getSenderVpa() {
        return senderVpa;
    }

    // Setter for senderVpa - allows other code to WRITE/update the sender's VPA
    // Example: request.setSenderVpa("alice@icici")
    public void setSenderVpa(String senderVpa) {
        this.senderVpa = senderVpa;
    }

    // Getter for receiverVpa - reads who receives the money
    public String getReceiverVpa() {
        return receiverVpa;
    }

    // Setter for receiverVpa - sets who receives the money
    public void setReceiverVpa(String receiverVpa) {
        this.receiverVpa = receiverVpa;
    }

    // Getter for amount - reads the transaction amount
    public BigDecimal getAmount() {
        return amount;
    }

    // Setter for amount - sets the transaction amount
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    // Getter for pin - reads the PIN (for verification)
    public String getPin() {
        return pin;
    }

    // Setter for pin - sets the PIN
    public void setPin(String pin) {
        this.pin = pin;
    }
}