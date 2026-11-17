package com.example.issuersim.service;

import com.example.issuersim.config.AppProperties;
import com.example.issuersim.dto.*;
import com.example.issuersim.model.Account;
import com.example.issuersim.model.CardlessToken;
import com.example.issuersim.model.SimulatorSettings;
import com.example.issuersim.model.TransactionLog;
import com.example.issuersim.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WithdrawalService {
    private static final Logger log = LoggerFactory.getLogger(WithdrawalService.class);
    private static final DateTimeFormatter TDT_FORMAT = DateTimeFormatter.ofPattern("MMddHHmmss");
    private static final DateTimeFormatter SDT_FORMAT = DateTimeFormatter.ofPattern("MMdd");
    private final SecureRandom random = new SecureRandom();
    private final ObjectMapper objectMapper;

    private final AccountRepository accountRepo;
    private final TokenRepository tokenRepo;
    private final IdempotencyRepository idemRepo;
    private final ApprovalRepository approvalRepo;
    private final TransactionLogRepository logRepo;
    private final SettingsService settingsService;
    private final AppProperties props;

    public WithdrawalService(AccountRepository a, TokenRepository t, IdempotencyRepository i,
                             ApprovalRepository ap, TransactionLogRepository lr, 
                             SettingsService ss, AppProperties p, ObjectMapper om) {
        this.accountRepo = a; this.tokenRepo = t; this.idemRepo = i;
        this.approvalRepo = ap; this.logRepo = lr; 
        this.settingsService = ss; this.props = p;
        this.objectMapper = om;
    }

    public WithdrawResponse authorize(WithdrawRequest req) {
        long start = System.currentTimeMillis();
        
        // 1. Validasi Request berdasarkan Global Request Settings
        WithdrawResponse validationError = validateIncomingRequest(req, start);
        if (validationError != null) return validationError;

        String idemKey = req.idempotencyKey();
        
        Optional<CardlessToken> tOpt = tokenRepo.find(req.pan);
        if (tOpt.isEmpty()) return decline(req, "14", "Invalid PAN", start);
        CardlessToken token = tOpt.get();
        
        Optional<Account> aOpt = accountRepo.find(token.accountNo);
        if (aOpt.isEmpty()) return decline(req, "14", "Account not found", start);
        Account acc = aOpt.get();

        synchronized (acc) {
            Optional<WithdrawResponse> cached = idemRepo.find(idemKey);
            if (cached.isPresent()) {
                WithdrawResponse resp = cached.get();
                logRepo.save(createLog(req, resp, "Cached", start));
                return resp;
            }

            if (!"360".equals(req.currencyCode)) return decline(req, "12", "Invalid currency", start);
            if (!"ACTIVE".equals(token.status)) return decline(req, "14", "PAN not active", start);
            if (token.expiresAt.isBefore(Instant.now())) return decline(req, "54", "Expired PAN", start);
            if (!"ACTIVE".equals(acc.status)) return decline(req, "05", "Account blocked", start);

            long amt = req.amountCents();
            if (amt > token.singleLimit) return decline(req, "61", "Exceeds single transaction limit", start);
            if (acc.usedToday + amt > acc.dailyLimit) return decline(req, "61", "Exceeds daily limit", start);
            if (acc.balance < amt) return decline(req, "51", "Insufficient funds", start);

            SimulatorSettings settings = settingsService.getSettings();
            String forcedRc = settings.getDefaultResponseCode();

            if (!"00".equals(forcedRc)) {
                return decline(req, forcedRc, getMessageForCode(forcedRc), start);
            }

            acc.balance -= amt;
            acc.usedToday += amt;

            WithdrawResponse resp = buildBaseResponse(req, settings);
            resp.responseCode = "00";
            resp.responseMessage = "Approved";

            idemRepo.save(idemKey, resp);
            approvalRepo.save(req.stan, resp);
            logRepo.save(createLog(req, resp, req.source, start));
            return resp;
        }
    }

    // Logika Validasi Request
    private WithdrawResponse validateIncomingRequest(WithdrawRequest req, long start) {
        SimulatorSettings settings = settingsService.getSettings();

        // 1. Validasi ProcCode
        if (settings.getValProcCode() != null && !settings.getValProcCode().isEmpty()) {
            if (!settings.getValProcCode().equals(req.procCode)) {
                return decline(req, "12", "Invalid ProcCode", start);
            }
        }

        // 2. Validasi Amount (Setting dalam rupiah, Request dalam cents)
        if (settings.getValAmount() > 0) {
            long expectedCents = settings.getValAmount() * 100; 
            if (req.amountCents() != expectedCents) {
                return decline(req, "13", "Invalid Amount", start);
            }
        }

        // 3. Validasi Acquirer Bank (4 Digit)
        if (settings.getValAcquirerBank() != null && !settings.getValAcquirerBank().isEmpty()) {
            if (!settings.getValAcquirerBank().equals(req.acquirerId)) {
                return decline(req, "92", "Invalid Acquirer Bank", start);
            }
        }

        // 4. Validasi Issuer Bank (Ambil 4 digit pertama dari PAN)
        if (settings.getValIssuerBank() != null && !settings.getValIssuerBank().isEmpty()) {
            String panIssuer = req.pan != null && req.pan.length() >= 4 ? req.pan.substring(0, 4) : "";
            if (!settings.getValIssuerBank().equals(panIssuer)) {
                return decline(req, "91", "Issuer Unavailable", start);
            }
        }

        // 5. Validasi additionalData48 (TLV Format: PI04 + CI19 + OT06)
        String addData = req.additionalData48 != null ? req.additionalData48 : "";
        
        // Cek PI04
        if (!addData.startsWith("PI04W001")) {
            return decline(req, "12", "Invalid Additional Data (PI)", start);
        }

        // Cek CI19 (PAN harus sama dengan req.pan)
        Pattern ciPattern = Pattern.compile("CI19(\\d{19})");
        Matcher ciMatcher = ciPattern.matcher(addData);
        if (!ciMatcher.find() || !ciMatcher.group(1).equals(req.pan)) {
            return decline(req, "12", "Invalid Additional Data (CI/PAN)", start);
        }

        // Cek OT06 (OTP)
        if (settings.getValOtp() != null && !settings.getValOtp().isEmpty()) {
            Pattern otPattern = Pattern.compile("OT06(\\d{6})");
            Matcher otMatcher = otPattern.matcher(addData);
            String extractedOtp = otMatcher.find() ? otMatcher.group(1) : "";
            
            if (!settings.getValOtp().equals(extractedOtp)) {
                return decline(req, "12", "Invalid OTP", start);
            }
        }

        // Cek Nomor Telp (Jika diselipkan di PI04, atau kita bisa longgarkan)
        if (settings.getValPhoneNumber() != null && !settings.getValPhoneNumber().isEmpty()) {
            if (!addData.contains(settings.getValPhoneNumber())) {
                return decline(req, "12", "Invalid Phone Number", start);
            }
        }

        return null; // Lolos validasi
    }

    public ReversalResponse reverse(ReversalRequest req) {
        String originalStan = req.originalStan();
        Optional<WithdrawResponse> orig = originalStan == null ? Optional.empty() : approvalRepo.find(originalStan);

        ReversalResponse resp = new ReversalResponse();
        resp.mti = "0430";
        resp.tmsDateTime = LocalDateTime.now().format(TDT_FORMAT);

        if (orig.isEmpty()) {
            resp.responseCode = "25";
            resp.responseMessage = "Unable to locate record";
            logRepo.save(createLogReverse(req, resp, "REST"));
            return resp;
        }

        Optional<CardlessToken> token = tokenRepo.find(req.pan);
        if (token.isPresent()) {
            Optional<Account> acc = accountRepo.find(token.get().accountNo);
            if (acc.isPresent()) {
                long amt = Long.parseLong(req.amount);
                synchronized (acc.get()) {
                    acc.get().balance += amt;
                    acc.get().usedToday = Math.max(0, acc.get().usedToday - amt);
                }
            }
        }
        approvalRepo.remove(originalStan);
        resp.responseCode = "00";
        resp.responseMessage = "Approved";
        logRepo.save(createLogReverse(req, resp, "REST"));
        return resp;
    }

    private WithdrawResponse buildBaseResponse(WithdrawRequest req, SimulatorSettings settings) {
        WithdrawResponse resp = new WithdrawResponse();
        resp.mti = "0210";
        
        resp.procCode = (settings.getOverrideProcCode() != null && !settings.getOverrideProcCode().isEmpty()) 
                        ? settings.getOverrideProcCode() : req.procCode;

        if (settings.isOverrideAmount() && settings.getFixedAmount() != null) {
            resp.amount = settings.getFixedAmount();
        } else {
            resp.amount = req.amount;
        }

        LocalDateTime now = LocalDateTime.now();
        resp.tmsDateTime = now.format(TDT_FORMAT);
        resp.settlementDate = now.format(SDT_FORMAT);
        resp.authIDResponse = String.format("%06d", random.nextInt(1000000));
        
        if (settings.getCompanyCode() != null && !settings.getCompanyCode().isEmpty()) {
            resp.companyCode = settings.getCompanyCode();
        } else if (req.pan != null && req.pan.length() >= 4) {
            resp.companyCode = req.pan.substring(0, 4);
        } else {
            resp.companyCode = props.getCompanyCode();
        }

        if (req.pan != null && req.pan.length() >= 9) {
            resp.accountId = req.pan.substring(7);
        }
        return resp;
    }

    private WithdrawResponse decline(WithdrawRequest req, String code, String msg, long start) {
        SimulatorSettings settings = settingsService.getSettings();
        WithdrawResponse resp = buildBaseResponse(req, settings);
        
        resp.responseCode = code;
        resp.responseMessage = msg;
        
        idemRepo.save(req.idempotencyKey(), resp);
        logRepo.save(createLog(req, resp, req.source, start));
        return resp;
    }

    private String getMessageForCode(String code) {
        if (code == null) return "Unknown Error";
        return switch (code) {
            case "00" -> "Approved";
            case "05" -> "Do Not Honor";
            case "12" -> "Invalid Transaction";
            case "13" -> "Invalid Amount";
            case "14" -> "Invalid PAN";
            case "51" -> "Insufficient Funds";
            case "54" -> "Expired PAN";
            case "61" -> "Exceeds Limit";
            case "91" -> "Issuer Unavailable";
            case "92" -> "Invalid Acquirer";
            case "96" -> "System Malfunction";
            default -> "Unknown Error";
        };
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() < 10) return pan;
        return pan.substring(0, 6) + "******" + pan.substring(pan.length() - 4);
    }

    private TransactionLog createLog(WithdrawRequest req, WithdrawResponse resp, String source, long duration) {
        String rawReq = "{}";
        String rawRes = "{}";
        try {
            rawReq = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(req);
            rawRes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resp);
        } catch (Exception e) {}
        
        return new TransactionLog(req.mti, "CCW", source, req.stan, req.rrn, maskPan(req.pan), resp.procCode, resp.amount, 
                resp.responseCode, resp.responseMessage, resp.companyCode, duration, rawReq, rawRes);
    }

    private TransactionLog createLogReverse(ReversalRequest req, ReversalResponse resp, String source) {
        String rawReq = "{}";
        String rawRes = "{}";
        try {
            rawReq = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(req);
            rawRes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resp);
        } catch (Exception e) {}
        
        return new TransactionLog("0420", "CCW REVERSAL", source, req.stan, req.rrn, maskPan(req.pan), req.procCode, req.amount, 
                resp.responseCode, resp.responseMessage, "", 0, rawReq, rawRes);
    }
}