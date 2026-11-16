package com.example.issuersim.iso8583;

import com.example.issuersim.service.WithdrawalService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Iso8583Server {
    private static final Logger log = LoggerFactory.getLogger(Iso8583Server.class);

    @Value("${iso8583.enabled:true}") private boolean enabled;
    @Value("${iso8583.port:8583}") private int port;
    @Value("${iso8583.packager-path:/iso8583/packager.xml}") private String packagerPath;
    @Value("${iso8583.length-header-bytes:2}") private int headerBytes;

    private final WithdrawalService withdrawalService;
    private ServerSocket serverSocket;
    private final ExecutorService pool = Executors.newFixedThreadPool(16);
    private volatile boolean running;

    public Iso8583Server(WithdrawalService w) { this.withdrawalService = w; }

    @PostConstruct
    public void start() throws Exception {
        if (!enabled) { log.info("ISO8583 server disabled"); return; }
        ISOPackager packager;
        try (InputStream in = getClass().getResourceAsStream(packagerPath)) {
            if (in == null) throw new IllegalStateException("packager not found: " + packagerPath);
            packager = new GenericPackager(in);
        }
        Iso8583Handler handler = new Iso8583Handler(packager, withdrawalService, headerBytes);
        serverSocket = new ServerSocket(port);
        running = true;
        Thread t = new Thread(() -> {
            log.info("ISO8583 listening port={}", port);
            while (running) {
                try {
                    Socket s = serverSocket.accept();
                    log.info("ISO8583 client connected {}", s.getRemoteSocketAddress());
                    pool.submit(() -> handler.handle(s));
                } catch (Exception e) {
                    if (running) log.warn("accept failed", e);
                }
            }
        }, "iso8583-acceptor");
        t.setDaemon(true);
        t.start();
    }

    @PreDestroy
    public void stop() {
        running = false;
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignored) {}
        pool.shutdownNow();
        log.info("ISO8583 server stopped");
    }
}