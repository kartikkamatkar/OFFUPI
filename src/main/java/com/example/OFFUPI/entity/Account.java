// SUMMARY: This entity represents a bank account in the database.
// It maps to the "accounts" table and stores user account information like VPA, name, and balance.
// VPA (Virtual Payment Address) is like an email address for payments - example: "alice@demo"

package com.example.OFFUPI.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

// @Entity tells JPA (Java Persistence API): "This class is a database table"
// Spring will automatically create/update the table based on this class
@Entity

// @Table specifies the exact name of the database table
// Without this, JPA would use the class name "Account" as table name
@Table(name = "accounts")
public class Account {

    // @Id marks this field as the PRIMARY KEY of the table
    // Each account is uniquely identified by its VPA (no two accounts can have same VPA)
    @Id
    private String vpa; // Virtual Payment Address, e.g. "alice@demo"

    // @Column maps this field to a database column
    // nullable = false means this field cannot be NULL in the database
    @Column(nullable = false)
    private String holderName;

    // @Column with precision and scale defines how decimals are stored
    // precision = 19 means total 19 digits can be stored
    // scale = 2 means 2 digits after decimal point (for paise/cents)
    // Example: 12345678901234567.89 (19 digits total, 2 decimal places)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    // @Version enables optimistic locking - prevents lost updates
    // When two transactions try to update the same account simultaneously,
    // only one succeeds. The other gets an exception and must retry.
    // Version number automatically increments on every update
    @Version  // Optimistic locking — prevents lost updates on concurrent transfers
    private Long version;

    // Default constructor (required by JPA for creating objects from database results)
    // JPA needs this to create empty objects and then fill them via reflection
    public Account() {}

    // Convenience constructor for easily creating new accounts
    public Account(String vpa, String holderName, BigDecimal balance) {
        this.vpa = vpa;
        this.holderName = holderName;
        this.balance = balance;
        // version is not set here - it starts as null and database sets initial value
    }

    // Getters and setters - allow controlled access to private fields
    // JPA uses these to read/write values from/to the database
    public String getVpa() { return vpa; }
    public void setVpa(String vpa) { this.vpa = vpa; }

    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}