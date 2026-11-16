package com.example.issuersim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.example.issuersim.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class CardlessIssuerSimApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardlessIssuerSimApplication.class, args);
    }
}