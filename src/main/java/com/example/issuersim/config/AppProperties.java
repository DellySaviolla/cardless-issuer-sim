package com.example.issuersim.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "issuer")
public class AppProperties {
    private String id = "ISSUER-SIM-001";
    private String companyCode = "0001";
    private String acquirerId = "0001";
    private String secretKey = "SoaJalin123";
    private boolean validateSignature = false;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String v) { this.companyCode = v; }
    public String getAcquirerId() { return acquirerId; }
    public void setAcquirerId(String v) { this.acquirerId = v; }
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String v) { this.secretKey = v; }
    public boolean isValidateSignature() { return validateSignature; }
    public void setValidateSignature(boolean v) { this.validateSignature = v; }
}