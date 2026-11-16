package com.example.issuersim.config;

import com.example.issuersim.dto.ResponseRule;
import com.example.issuersim.model.Account;
import com.example.issuersim.model.CardlessToken;
import com.example.issuersim.repository.AccountRepository;
import com.example.issuersim.repository.TokenRepository;
import com.example.issuersim.service.RuleEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Configuration
public class DataSeeder {

    @Bean
    public ApplicationRunner seed(AccountRepository accRepo, TokenRepository tokenRepo,
                                  RuleEngine engine) {
        return (ApplicationArguments args) -> {
            accRepo.save(new Account("ACC001", "081123456789",
                    100_000_000L, 200_000_000L, "ACTIVE"));
            
            // Perbaikan: Token berlaku 365 hari
            tokenRepo.save(new CardlessToken("0849000081123456789", "ACC001", "ACTIVE",
                    Instant.now().plus(365, ChronoUnit.DAYS),
                    50_000_000L, 200_000_000L));

            try (InputStream in = getClass().getResourceAsStream("/response-rules.yml")) {
                if (in != null) {
                    Map<String, Object> root = new Yaml().load(in);
                    Object rules = root.get("rules");
                    if (rules instanceof List<?> list) {
                        ObjectMapper om = new ObjectMapper();
                        List<ResponseRule> parsed = list.stream()
                                .map(o -> om.convertValue(o, ResponseRule.class))
                                .toList();
                        engine.setRules(parsed);
                    }
                }
            }
        };
    }
}