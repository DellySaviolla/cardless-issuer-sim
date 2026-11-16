package com.example.issuersim.controller;

import com.example.issuersim.config.AppProperties;
import com.example.issuersim.dto.ReversalRequest;
import com.example.issuersim.dto.ReversalResponse;
import com.example.issuersim.dto.WithdrawRequest;
import com.example.issuersim.dto.WithdrawResponse;
import com.example.issuersim.service.SignatureUtil;
import com.example.issuersim.service.WithdrawalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class WithdrawalController {
    private static final Logger log = LoggerFactory.getLogger(WithdrawalController.class);

    private final WithdrawalService svc;
    private final AppProperties props;
    private final ObjectMapper mapper;
    private final Validator validator; // Inject Validator

    public WithdrawalController(WithdrawalService svc, AppProperties props, ObjectMapper mapper, Validator validator) {
        this.svc = svc; this.props = props; this.mapper = mapper; this.validator = validator;
    }

    @PostMapping("/{network}/jalin/openapi/cardless/withdrawaloffus/debit")
    public ResponseEntity<WithdrawResponse> debit(
            @PathVariable String network,
            @RequestHeader(value = "cid", required = false) String cid,
            @RequestHeader(value = "Signature", required = false) String signature,
            @RequestBody String rawBody) throws Exception {

        validateSignature(cid, signature, rawBody);
        WithdrawRequest req = mapper.readValue(rawBody, WithdrawRequest.class);
        
        var violations = validator.validate(req);
        if (!violations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, violations.toString());
        }
        
        req.source = "REST";
        log.info("withdrawal debit network={} stan={} rrn={}", network, req.stan, req.rrn);
        return ResponseEntity.ok(svc.authorize(req));
    }

    @PostMapping("/{network}/jalin/openapi/cardless/withdrawaloffus/reversal")
    public ResponseEntity<ReversalResponse> reversal(
            @PathVariable String network,
            @RequestHeader(value = "cid", required = false) String cid,
            @RequestHeader(value = "Signature", required = false) String signature,
            @RequestBody String rawBody) throws Exception {

        validateSignature(cid, signature, rawBody);
        ReversalRequest req = mapper.readValue(rawBody, ReversalRequest.class);
        
        var violations = validator.validate(req);
        if (!violations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, violations.toString());
        }
        
        log.info("withdrawal reversal network={} stan={} originalStan={}", network, req.stan, req.originalStan());
        return ResponseEntity.ok(svc.reverse(req));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() { return ResponseEntity.ok("{\"status\":\"UP\"}"); }

    private void validateSignature(String cid, String signature, String rawBody) {
        if (!props.isValidateSignature()) return;
        if (cid == null || signature == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing cid or Signature header");
        }
        String expected = SignatureUtil.sha512Hex(cid + ":" + rawBody + ":" + props.getSecretKey());
        if (!expected.equalsIgnoreCase(signature)) {
            log.warn("signature mismatch cid={}", cid);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid signature");
        }
    }
}