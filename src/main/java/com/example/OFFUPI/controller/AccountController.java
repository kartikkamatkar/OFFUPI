package com.example.OFFUPI.controller;

import com.example.OFFUPI.entity.Account;
import com.example.OFFUPI.repository.AccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountRepository repository;

    @GetMapping
    public List<Account> getAll() {

        return repository.findAll();
    }
}