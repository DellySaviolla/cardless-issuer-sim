package com.example.issuersim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class ReversalRequest {
    @NotBlank @Pattern(regexp = "0420|0421") public String mti;
    @NotBlank public String pan;
    @NotBlank public String procCode;
    @NotBlank public String amount;
    @NotBlank public String tmsDateTime;
    @NotBlank public String stan;
    @NotBlank public String localTime;
    @NotBlank public String localDate;
    @NotBlank public String settlementDate;
    @NotBlank public String captureDate;
    @NotBlank public String channelCode;
    @NotBlank public String posEntryMode;
    @NotBlank public String acquirerId;
    @NotBlank public String rrn;
    @NotBlank public String authIDResponse;
    @NotBlank public String responseCode;
    @NotBlank public String terminalId;
    @NotBlank public String terminalInfo;
    public String additionalData48;
    @NotBlank public String currencyCode;
    @NotBlank public String terminalData;
    @NotBlank @Size(min = 42, max = 42) public String originalDataElements;
    @NotBlank public String companyCode;
    public String accountId;

    public String originalStan() {
        if (originalDataElements == null || originalDataElements.length() < 10) return null;
        return originalDataElements.substring(4, 10);
    }
}