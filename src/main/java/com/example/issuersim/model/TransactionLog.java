package com.example.issuersim.model;

import java.time.LocalDateTime;

public class TransactionLog {
    public LocalDateTime timestamp;
    public String mti;
    public String transactionType; // Tambahan: Transaksi (CCW, Transfer, dll)
    public String source;          // Tambahan: Jenis Message (REST, ISO8583)
    public String stan;
    public String rrn;
    public String pan;
    public String procCode;
    public String amount;
    public String responseCode;
    public String responseMessage;
    public String companyCode;
    public long durationMs;
    public String rawRequest;
    public String rawResponse;

    public TransactionLog(String mti, String transactionType, String source, String stan, String rrn, 
                          String pan, String procCode, String amount, String responseCode, String responseMessage, 
                          String companyCode, long durationMs, String rawRequest, String rawResponse) {
        this.timestamp = LocalDateTime.now();
        this.mti = mti;
        this.transactionType = transactionType;
        this.source = source;
        this.stan = stan;
        this.rrn = rrn;
        this.pan = pan;
        this.procCode = procCode;
        this.amount = amount;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.companyCode = companyCode;
        this.durationMs = durationMs;
        this.rawRequest = rawRequest;
        this.rawResponse = rawResponse;
    }
}