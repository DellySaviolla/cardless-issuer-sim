package com.example.issuersim;

import com.example.issuersim.config.AppProperties;
import com.example.issuersim.dto.*;
import com.example.issuersim.model.*;
import com.example.issuersim.repository.*;
import com.example.issuersim.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import static org.junit.jupiter.api.Assertions.*;

class WithdrawalServiceTest {

    private WithdrawalService buildSvc() {
        AccountRepository accRepo = new AccountRepository();
        TokenRepository tokenRepo = new TokenRepository();
        IdempotencyRepository idemRepo = new IdempotencyRepository();
        ApprovalRepository approvalRepo = new ApprovalRepository();
        TransactionLogRepository logRepo = new TransactionLogRepository();
        SettingsService settingsService = new SettingsService();
        ObjectMapper objectMapper = new ObjectMapper(); // Tambahan baru
        
        accRepo.save(new Account("ACC001", "081123456789", 100_000_000L, 200_000_000L, "ACTIVE"));
        tokenRepo.save(new CardlessToken("0849000081123456789", "ACC001", "ACTIVE",
                Instant.now().plus(1, ChronoUnit.HOURS), 50_000_000L, 200_000_000L));
                
        return new WithdrawalService(accRepo, tokenRepo, idemRepo, approvalRepo, logRepo, settingsService, new AppProperties(), objectMapper);
    }

    private WithdrawRequest req() {
        WithdrawRequest r = new WithdrawRequest();
        r.mti = "0200"; r.pan = "0849000081123456789"; r.procCode = "320000";
        r.amount = "000010000000"; r.tmsDateTime = "0831094752"; r.stan = "001880";
        r.localTime = "164752"; r.localDate = "0831"; r.captureDate = "0831";
        r.channelCode = "6011"; r.posEntryMode = "000"; r.acquirerId = "0002";
        r.rrn = "084288414383"; r.terminalId = "T0200002"; r.terminalInfo = "ATM BRI";
        r.additionalData48 = "PI04W001CI19"; r.currencyCode = "360"; r.terminalData = "BRI 0002+120";
        r.source = "TEST";
        return r;
    }

    @Test void approved() {
        WithdrawResponse r = buildSvc().authorize(req());
        assertEquals("00", r.responseCode);
        assertEquals("0210", r.mti);
        assertNotNull(r.authIDResponse);
    }

    @Test void idempotency() {
        WithdrawalService svc = buildSvc();
        WithdrawResponse a = svc.authorize(req());
        WithdrawResponse b = svc.authorize(req());
        assertSame(a, b);
    }
}