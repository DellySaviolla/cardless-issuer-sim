package com.example.issuersim.repository;

import com.example.issuersim.model.Account;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class AccountRepository {
    private final ConcurrentHashMap<String, Account> store = new ConcurrentHashMap<>();
    public Optional<Account> find(String accountNo) { return Optional.ofNullable(store.get(accountNo)); }
    public void save(Account acc) { store.put(acc.accountNo, acc); }
}