package com.example.issuersim.repository;

import com.example.issuersim.dto.WithdrawResponse;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class IdempotencyRepository {
    private final ConcurrentHashMap<String, WithdrawResponse> store = new ConcurrentHashMap<>();
    public Optional<WithdrawResponse> find(String requestId) { return Optional.ofNullable(store.get(requestId)); }
    public void save(String requestId, WithdrawResponse resp) { store.putIfAbsent(requestId, resp); }
    public void clear() { store.clear(); }
}