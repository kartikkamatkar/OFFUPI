package com.example.OFFUPI.repository;

import com.example.OFFUPI.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, String> {
}