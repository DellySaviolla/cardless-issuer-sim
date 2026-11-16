package com.example.issuersim.controller;

import com.example.issuersim.model.SimulatorSettings;
import com.example.issuersim.model.TransactionLog;
import com.example.issuersim.repository.IdempotencyRepository;
import com.example.issuersim.repository.TransactionLogRepository;
import com.example.issuersim.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminRuleController {
    private final TransactionLogRepository logRepo;
    private final IdempotencyRepository idemRepo;
    private final SettingsService settingsService;

    public AdminRuleController(TransactionLogRepository logRepo, IdempotencyRepository idemRepo, SettingsService settingsService) {
        this.logRepo = logRepo;
        this.idemRepo = idemRepo;
        this.settingsService = settingsService;
    }

    @GetMapping("/transactions")
    public List<TransactionLog> getTransactions() { return logRepo.findAllDescending(); }

    @PostMapping("/clear-cache")
    public ResponseEntity<String> clearCache() {
        idemRepo.clear();
        logRepo.clear();
        return ResponseEntity.ok("Cache and logs cleared");
    }

    @GetMapping("/settings")
    public SimulatorSettings getSettings() { return settingsService.getSettings(); }

    @PostMapping("/settings")
    public ResponseEntity<SimulatorSettings> updateSettings(@RequestBody SimulatorSettings settings) {
        settingsService.updateSettings(settings);
        return ResponseEntity.ok(settings);
    }
}