package com.example.issuersim.repository;

import com.example.issuersim.model.TransactionLog;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class TransactionLogRepository {
    private final List<TransactionLog> logs = Collections.synchronizedList(new ArrayList<>());
    private static final int MAX_LOGS = 500;

    public void save(TransactionLog log) {
        if (logs.size() >= MAX_LOGS) { logs.remove(0); }
        logs.add(log);
    }

    public List<TransactionLog> findAllDescending() {
        List<TransactionLog> copy = new ArrayList<>(logs);
        Collections.reverse(copy);
        return copy;
    }
    
    public void clear() { logs.clear(); }
}