package com.example.issuersim.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class WithdrawRequest {
    @NotBlank @Pattern(regexp = "0200") public String mti;
    @NotBlank @Size(max = 19) public String pan;
    @NotBlank @Size(min = 6, max = 6) public String procCode;
    @NotBlank @Pattern(regexp = "\\d{12}") public String amount;
    @NotBlank @Pattern(regexp = "\\d{10}") public String tmsDateTime;
    @NotBlank @Size(min = 6, max = 6) public String stan;
    @NotBlank @Size(min = 6, max = 6) public String localTime;
    @NotBlank @Size(min = 4, max = 4) public String localDate;
    @NotBlank @Size(min = 4, max = 4) public String captureDate;
    @NotBlank @Size(min = 4, max = 4) public String channelCode;
    @NotBlank @Size(min = 3, max = 3) public String posEntryMode;
    @NotBlank @Size(max = 11) public String acquirerId;
    @NotBlank @Size(min = 12, max = 12) public String rrn;
    @NotBlank @Size(max = 16) public String terminalId;
    @NotBlank @Size(max = 40) public String terminalInfo;
    public String additionalData48;
    @NotBlank @Size(min = 3, max = 3) public String currencyCode;
    @NotBlank @Size(max = 12) public String terminalData;

    // internal
    public String source;

    public long amountCents() {
        try { return Long.parseLong(amount); }
        catch (NumberFormatException e) { return 0L; }
    }

    public String idempotencyKey() {
        String datePart = (tmsDateTime != null && tmsDateTime.length() >= 4) ? tmsDateTime.substring(0, 4) : "";
        return datePart + "|" + (rrn == null ? "" : rrn);
    }
}