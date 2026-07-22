package com.mgt.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class OtpService {
    private static final long TTL_SECONDS = 300;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public String create(String mobile) {
        String key = normalize(mobile);
        if (key.length() < 10) throw new IllegalArgumentException("Invalid mobile number");
        String otp = String.format("%06d", random.nextInt(1_000_000));
        store.put(key, new Entry(otp, Instant.now().plusSeconds(TTL_SECONDS)));
        return otp;
    }

    public boolean verify(String mobile, String otp) {
        String key = normalize(mobile);
        Entry entry = store.get(key);
        if (entry == null || Instant.now().isAfter(entry.expiresAt())) {
            store.remove(key);
            return false;
        }
        boolean ok = entry.otp().equals(otp == null ? "" : otp.trim());
        if (ok) store.remove(key);
        return ok;
    }

    private String normalize(String mobile) {
        return mobile == null ? "" : mobile.replaceAll("[^0-9+]", "").trim();
    }

    private record Entry(String otp, Instant expiresAt) {}
}
