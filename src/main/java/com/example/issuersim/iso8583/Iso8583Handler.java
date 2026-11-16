package com.example.issuersim.iso8583;

import com.example.issuersim.dto.WithdrawRequest;
import com.example.issuersim.dto.WithdrawResponse;
import com.example.issuersim.service.WithdrawalService;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Iso8583Handler {
    private static final Logger log = LoggerFactory.getLogger(Iso8583Handler.class);
    private static final DateTimeFormatter TDT = DateTimeFormatter.ofPattern("MMddHHmmss");
    private static final DateTimeFormatter HHM = DateTimeFormatter.ofPattern("HHmmss");
    private static final DateTimeFormatter MMDD = DateTimeFormatter.ofPattern("MMdd");

    private final ISOPackager packager;
    private final WithdrawalService svc;
    private final int headerBytes;

    public Iso8583Handler(ISOPackager packager, WithdrawalService svc, int headerBytes) {
        this.packager = packager; this.svc = svc; this.headerBytes = headerBytes;
    }

    public void handle(Socket socket) {
        try (socket) {
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            while (true) {
                byte[] payload;
                try { payload = readFrame(in); } catch (EOFException eof) { return; }
                ISOMsg isoReq = new ISOMsg();
                isoReq.setPackager(packager);
                isoReq.unpack(payload);
                log.info("ISO8583 RECV mti={} stan={} pan={}", isoReq.getMTI(), isoReq.getString(11), isoReq.getString(2));

                ISOMsg isoResp = process(isoReq);
                writeFrame(out, isoResp.pack());
            }
        } catch (Exception e) {
            log.warn("ISO8583 connection error", e);
        }
    }

    private ISOMsg process(ISOMsg isoReq) throws ISOException {
        WithdrawRequest req = new WithdrawRequest();
        req.source = "ISO8583";
        req.mti = "0200";
        req.pan = isoReq.getString(2);
        req.procCode = isoReq.getString(3);
        req.amount = isoReq.getString(4);
        req.tmsDateTime = isoReq.getString(7);
        req.stan = isoReq.getString(11);
        req.localTime = LocalDateTime.now().format(HHM);
        req.localDate = LocalDateTime.now().format(MMDD);
        req.captureDate = req.localDate;
        req.channelCode = "6011";
        req.posEntryMode = "000";
        req.acquirerId = "0001";
        req.rrn = isoReq.getString(37);
        req.terminalId = isoReq.getString(41);
        req.terminalInfo = isoReq.getString(42);
        req.additionalData48 = "";
        req.currencyCode = isoReq.getString(49);
        req.terminalData = "MDR 1234+120";

        WithdrawResponse wresp = svc.authorize(req);

        ISOMsg resp = new ISOMsg();
        resp.setPackager(packager);
        resp.setMTI("0210");
        resp.set(2, req.pan);
        resp.set(3, wresp.procCode);
        resp.set(4, wresp.amount);
        resp.set(7, LocalDateTime.now().format(TDT));
        resp.set(11, req.stan);
        resp.set(37, req.rrn);
        resp.set(38, wresp.authIDResponse != null ? wresp.authIDResponse : "000000");
        resp.set(39, wresp.responseCode);
        resp.set(41, req.terminalId);
        resp.set(42, req.terminalInfo != null ? req.terminalInfo : "");
        resp.set(49, "360");
        log.info("ISO8583 SEND mti=0210 rc={}", wresp.responseCode);
        return resp;
    }

    private byte[] readFrame(DataInputStream in) throws IOException {
        if (headerBytes == 2) { int len = in.readUnsignedShort(); byte[] b = new byte[len]; in.readFully(b); return b; }
        int len = in.readInt(); byte[] b = new byte[len]; in.readFully(b); return b;
    }

    private void writeFrame(DataOutputStream out, byte[] payload) throws IOException {
        if (headerBytes == 2) out.writeShort(payload.length); else out.writeInt(payload.length);
        out.write(payload); out.flush();
    }
}