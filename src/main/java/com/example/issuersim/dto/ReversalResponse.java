package com.example.issuersim.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReversalResponse {
    public String mti;
    public String tmsDateTime;
    public String responseCode;
    public String responseMessage;
}