package com.example.issuersim.model;

import java.time.Instant;

public class CardlessToken {
    public final String pan;
    public final String accountNo;
    public volatile String status;
    public volatile Instant expiresAt;
    public volatile long singleLimit;
    public volatile long dailyLimit;

    public CardlessToken(String pan, String accountNo, String status,
                         Instant expiresAt, long singleLimit, long dailyLimit) {
        this.pan = pan;
        this.accountNo = accountNo;
        this.status = status;
        this.expiresAt = expiresAt;
        this.singleLimit = singleLimit;
        this.dailyLimit = dailyLimit;
    }
}