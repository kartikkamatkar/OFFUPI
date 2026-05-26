package com.example.OFFUPI.controller;

import com.example.OFFUPI.entity.Transaction;
import com.example.OFFUPI.repository.TransactionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository repository;

    @GetMapping
    public List<Transaction> transactions() {

        return repository.findTop20ByOrderByIdDesc();
    }
}