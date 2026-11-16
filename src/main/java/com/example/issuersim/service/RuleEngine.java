package com.example.issuersim.service;

import com.example.issuersim.dto.ResponseRule;
import com.example.issuersim.dto.RuleMatch;
import com.example.issuersim.dto.WithdrawRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class RuleEngine {
    private final CopyOnWriteArrayList<ResponseRule> rules = new CopyOnWriteArrayList<>();

    public void setRules(List<ResponseRule> initial) {
        rules.clear();
        if (initial != null) rules.addAll(initial);
    }
    public List<ResponseRule> list() { return List.copyOf(rules); }
    public void add(ResponseRule r) { rules.add(r); }
    public boolean delete(String id) { return rules.removeIf(r -> r.id != null && r.id.equals(id)); }

    public Optional<ResponseRule> match(WithdrawRequest req) {
        for (ResponseRule r : rules) {
            if (matches(r.match, req)) return Optional.of(r);
        }
        return Optional.empty();
    }

    private boolean matches(RuleMatch m, WithdrawRequest req) {
        if (m == null) return true;
        long amt = req.amountCents();
        if (m.terminalId != null && !m.terminalId.equals(req.terminalId)) return false;
        if (m.acquirerId != null && !m.acquirerId.equals(req.acquirerId)) return false;
        if (m.pan != null && !m.pan.equals(req.pan)) return false;
        if (m.stan != null && !m.stan.equals(req.stan)) return false;
        if (m.rrn != null && !m.rrn.equals(req.rrn)) return false;
        if (m.channelCode != null && !m.channelCode.equals(req.channelCode)) return false;
        if (m.amountEq != null && amt != m.amountEq) return false;
        if (m.amountGte != null && amt < m.amountGte) return false;
        if (m.amountLt != null && amt >= m.amountLt) return false;
        return true;
    }
}