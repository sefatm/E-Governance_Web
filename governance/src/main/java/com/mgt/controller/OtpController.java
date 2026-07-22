package com.mgt.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgt.service.OtpService;

@RestController
@RequestMapping("/api/otp")
public class OtpController {
    private final OtpService otpService;

    @Value("${app.otp.dev-mode:true}")
    private boolean devMode;

    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/send")
    public ResponseEntity<Object> send(@RequestBody Map<String, String> body) {
        try {
            String mobile = body.get("mobile");
            String otp = otpService.create(mobile);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "OTP generated successfully");
            response.put("expiresInSeconds", 300);
            if (devMode) response.put("devOtp", otp);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Object> verify(@RequestBody Map<String, String> body) {
        boolean valid = otpService.verify(body.get("mobile"), body.get("otp"));
        if (!valid) return ResponseEntity.badRequest().body(Map.of("verified", false, "message", "Invalid or expired OTP"));
        return ResponseEntity.ok(Map.of("verified", true, "message", "OTP verified"));
    }
}
