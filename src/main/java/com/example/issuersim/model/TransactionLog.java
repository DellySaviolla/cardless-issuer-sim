package com.example.issuersim.model;

import java.time.LocalDateTime;

public class TransactionLog {
    public LocalDateTime timestamp;
    public String mti;
    public String stan;
    public String pan;
    public String procCode;
    public String amount;
    public String responseCode;
    public String responseMessage;
    public String companyCode;
    public String source;
    public long durationMs;
    
    // Field baru untuk menyimpan JSON mentah
    public String rawRequest;
    public String rawResponse;

    public TransactionLog(String mti, String stan, String pan, String procCode, String amount, 
                          String responseCode, String responseMessage, String companyCode, String source, 
                          long durationMs, String rawRequest, String rawResponse) {
        this.timestamp = LocalDateTime.now();
        this.mti = mti;
        this.stan = stan;
        this.pan = pan;
        this.procCode = procCode;
        this.amount = amount;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.companyCode = companyCode;
        this.source = source;
        this.durationMs = durationMs;
        this.rawRequest = rawRequest;
        this.rawResponse = rawResponse;
    }
}