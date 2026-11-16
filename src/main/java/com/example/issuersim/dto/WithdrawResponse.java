package com.example.issuersim.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WithdrawResponse {
    public String mti;
    public String procCode;
    public String amount;
    public String tmsDateTime;
    public String settlementDate;
    public String authIDResponse;
    public String responseCode;
    public String responseMessage;
    public String companyCode;
    public String accountId;
}