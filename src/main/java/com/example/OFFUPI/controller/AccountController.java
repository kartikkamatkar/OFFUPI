// SUMMARY: This controller handles API requests for bank accounts.
// It provides endpoints to get account information from the database.
// URL pattern: /api/accounts - all endpoints in this class start with this path.

package com.example.OFFUPI.controller;

import com.example.OFFUPI.entity.Account;
import com.example.OFFUPI.repository.AccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController tells Spring: "This class handles web requests and returns data (not HTML pages)"
// It automatically converts Java objects to JSON format for the response
@RestController

// @RequestMapping sets the base URL path for ALL methods in this controller
// All endpoints here will start with: http://your-server.com/api/accounts
@RequestMapping("/api/accounts")
public class AccountController {

    // @Autowired tells Spring: "Automatically connect the AccountRepository here"
    // Spring will find the repository bean and inject it automatically
    // This is called Dependency Injection - Spring gives us the object we need
    @Autowired
    private AccountRepository repository;

    // This method handles GET requests to /api/accounts
    // Purpose: Fetch ALL accounts from the database and return them as JSON
    @GetMapping
    public List<Account> getAll() {

        // repository.findAll() goes to the database and retrieves every Account record
        // Spring automatically converts the List<Account> into JSON format
        // No need to write SQL - Spring Data JPA does it for us
        return repository.findAll();
    }
}