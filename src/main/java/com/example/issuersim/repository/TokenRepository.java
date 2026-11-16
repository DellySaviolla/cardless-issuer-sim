package com.example.issuersim.repository;

import com.example.issuersim.model.CardlessToken;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TokenRepository {
    private final ConcurrentHashMap<String, CardlessToken> store = new ConcurrentHashMap<>();
    public Optional<CardlessToken> find(String pan) { return Optional.ofNullable(store.get(pan)); }
    public void save(CardlessToken t) { store.put(t.pan, t); }
}