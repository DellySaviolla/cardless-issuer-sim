package com.example.issuersim.model;

public class SimulatorSettings {
    // --- RESPONSE SETTINGS ---
    private String defaultResponseCode = "00";
    private String companyCode = "0849";
    private String overrideProcCode = "326000"; 
    private boolean overrideAmount = false;
    private String fixedAmount = "000010000000";

    // --- REQUEST VALIDATION SETTINGS (BARU) ---
    private String valIssuerBank = "0849";       // 4 digit awal PAN
    private String valAcquirerBank = "0002";     // acquirerId (4 digit)
    private String valPhoneNumber = "081123456789";
    private String valOtp = "123456";
    private String valProcCode = "320000";
    private long valAmount = 100000;             // Rp 100.000

    // Getters and Setters Response
    public String getDefaultResponseCode() { return defaultResponseCode; }
    public void setDefaultResponseCode(String defaultResponseCode) { this.defaultResponseCode = defaultResponseCode; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
    public String getOverrideProcCode() { return overrideProcCode; }
    public void setOverrideProcCode(String overrideProcCode) { this.overrideProcCode = overrideProcCode; }
    public boolean isOverrideAmount() { return overrideAmount; }
    public void setOverrideAmount(boolean overrideAmount) { this.overrideAmount = overrideAmount; }
    public String getFixedAmount() { return fixedAmount; }
    public void setFixedAmount(String fixedAmount) { this.fixedAmount = fixedAmount; }

    // Getters and Setters Validation
    public String getValIssuerBank() { return valIssuerBank; }
    public void setValIssuerBank(String valIssuerBank) { this.valIssuerBank = valIssuerBank; }
    public String getValAcquirerBank() { return valAcquirerBank; }
    public void setValAcquirerBank(String valAcquirerBank) { this.valAcquirerBank = valAcquirerBank; }
    public String getValPhoneNumber() { return valPhoneNumber; }
    public void setValPhoneNumber(String valPhoneNumber) { this.valPhoneNumber = valPhoneNumber; }
    public String getValOtp() { return valOtp; }
    public void setValOtp(String valOtp) { this.valOtp = valOtp; }
    public String getValProcCode() { return valProcCode; }
    public void setValProcCode(String valProcCode) { this.valProcCode = valProcCode; }
    public long getValAmount() { return valAmount; }
    public void setValAmount(long valAmount) { this.valAmount = valAmount; }
}