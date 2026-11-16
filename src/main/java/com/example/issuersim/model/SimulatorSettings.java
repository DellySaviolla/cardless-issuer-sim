package com.example.issuersim.model;

public class SimulatorSettings {
    private String defaultResponseCode = "00";
    private String companyCode = "0849";
    private String overrideProcCode = "326000"; 
    
    private boolean overrideAmount = false;
    private String fixedAmount = "000010000000";

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
}