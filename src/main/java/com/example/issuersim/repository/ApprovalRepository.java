package com.example.issuersim.repository;

import com.example.issuersim.dto.WithdrawResponse;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ApprovalRepository {
    private final ConcurrentHashMap<String, WithdrawResponse> byStan = new ConcurrentHashMap<>();

    public void save(String stan, WithdrawResponse resp) { byStan.put(stan, resp); }
    public Optional<WithdrawResponse> find(String stan) { return Optional.ofNullable(byStan.get(stan)); }
    public void remove(String stan) { byStan.remove(stan); }
}