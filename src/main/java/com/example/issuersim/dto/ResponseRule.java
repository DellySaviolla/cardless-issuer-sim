package com.example.issuersim.dto;

public class ResponseRule {
    public String id;
    public String description;
    public RuleMatch match = new RuleMatch();
    public String responseCode;
    public String responseMessage;
    public int delayMs;
}