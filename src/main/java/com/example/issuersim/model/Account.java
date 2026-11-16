package com.example.issuersim.model;

public class Account {
    public final String accountNo;
    public final String accountId;
    public volatile long balance;
    public volatile long usedToday;
    public volatile long dailyLimit;
    public volatile String status;

    public Account(String accountNo, String accountId, long balance, long dailyLimit, String status) {
        this.accountNo = accountNo;
        this.accountId = accountId;
        this.balance = balance;
        this.dailyLimit = dailyLimit;
        this.status = status;
    }
}