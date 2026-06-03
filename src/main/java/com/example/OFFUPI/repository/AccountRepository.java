// SUMMARY: This repository handles database operations for Account entities.
// It provides methods to save, find, delete, and update accounts without writing SQL.
// Spring Data JPA automatically implements this interface at runtime.

package com.example.OFFUPI.repository;

import com.example.OFFUPI.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

// This interface extends JpaRepository which provides standard CRUD methods
// JpaRepository<Account, String> means:
//   - Account: The entity type this repository manages
//   - String: The data type of the primary key (@Id field in Account)
//
// Spring Data JPA will automatically create an implementation class at runtime
// You don't need to write any method implementations!
public interface AccountRepository extends JpaRepository<Account, String> {

    // No custom methods needed here yet!
    // JpaRepository already provides these methods for free:
    // - save(Account) - save or update an account
    // - findById(String vpa) - find account by VPA
    // - findAll() - get all accounts
    // - deleteById(String vpa) - delete account
    // - count() - count total accounts
    // - existsById(String vpa) - check if account exists

}