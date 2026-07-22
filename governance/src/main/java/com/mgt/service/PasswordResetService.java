package com.mgt.service;

import com.mgt.dao.PasswordResetTokenDAO;
import com.mgt.dao.UserDAO;
import com.mgt.model.AppUser;
import com.mgt.model.PasswordResetToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {

    @Autowired private PasswordResetTokenDAO tokenDAO;
    @Autowired private UserDAO               userDAO;
    @Autowired private EmailService          emailService;
    @Autowired private PasswordEncoder       passwordEncoder;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    // ── OTP in-memory store ──────────────────────────────────────────────────
    // email → { otp, expiresAt }
    // Production-এ Redis ব্যবহার করা উচিত, কিন্তু এই project-এ ConcurrentHashMap যথেষ্ট
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static class OtpEntry {
        final String        otp;
        final LocalDateTime expiresAt;
        OtpEntry(String otp) {
            this.otp       = otp;
            this.expiresAt = LocalDateTime.now().plusMinutes(10); // 10 মিনিট valid
        }
        boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
    }

    // ── STEP 1: OTP generate করে email-এ পাঠাও ─────────────────────────────
    // FIX: আগে এই method empty (TODO stub) ছিল — কিছুই হচ্ছিল না
    public void sendOtp(String email) {
        AppUser user = userDAO.findByEmail(email);

        // Security: email না থাকলেও চুপ থাকো (user enumeration attack এড়াতে)
        // কিন্তু OTP পাঠানো হবে না
        if (user == null) return;
        if ("Inactive".equalsIgnoreCase(user.getStatus())) return;

        // 6-digit OTP generate (SecureRandom — Math.random() insecure)
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        // পুরনো OTP replace করো (একই email এ নতুন request এলে)
        otpStore.put(email, new OtpEntry(otp));

        // Email পাঠাও
        String subject  = "পাসওয়ার্ড রিসেট OTP — E-Governance Municipal Portal";
        String htmlBody = buildOtpEmailHtml(user.getName(), otp);
        emailService.sendHtml(email, subject, htmlBody);
    }

    // ── STEP 2: OTP verify করো ──────────────────────────────────────────────
    // FIX: আগে এই method empty ছিল — সবসময় null (success) return করছিল
    // ফলে যেকোনো OTP দিলেই verify হয়ে যেত কিন্তু পরের step-এ reset কাজ করত না
    public String verifyOtp(String email, String otp) {
        OtpEntry entry = otpStore.get(email);

        if (entry == null) {
            return "OTP পাওয়া যায়নি। আবার Send OTP করুন।";
        }
        if (entry.isExpired()) {
            otpStore.remove(email);
            return "OTP মেয়াদ শেষ হয়ে গেছে। আবার Send OTP করুন।";
        }
        if (!entry.otp.equals(otp)) {
            return "OTP সঠিক নয়। আবার চেষ্টা করুন।";
        }

        // verified — store-এ রাখো যাতে step 3-এ reset করতে পারে
        return null; // null = success
    }

    // ── STEP 3: OTP দিয়ে password reset করো ────────────────────────────────
    // FIX: আগে এই method empty ছিল — password update হচ্ছিল না
    public String resetWithOtp(String email, String otp, String newPassword) {
        // OTP আবার verify করো (step 2 bypass attack এড়াতে)
        OtpEntry entry = otpStore.get(email);

        if (entry == null)        return "Session শেষ হয়ে গেছে। আবার চেষ্টা করুন।";
        if (entry.isExpired()) {
            otpStore.remove(email);
            return "OTP মেয়াদ শেষ। আবার Send OTP করুন।";
        }
        if (!entry.otp.equals(otp)) return "OTP সঠিক নয়।";

        AppUser user = userDAO.findByEmail(email);
        if (user == null)         return "User পাওয়া যায়নি।";

        // Password update (BCrypt encode)
        userDAO.changePassword(user.getId(), passwordEncoder.encode(newPassword));

        // OTP একবার ব্যবহার হলে delete করো (single-use)
        otpStore.remove(email);

        return null; // null = success
    }

    // ── Token-based reset (alternative email-link flow) ──────────────────────
    public String initiateReset(String email) {
        AppUser user = userDAO.findByEmail(email);
        if (user == null) return null;
        if ("Inactive".equalsIgnoreCase(user.getStatus())) return null;

        tokenDAO.deleteByEmail(email);
        String rawToken = UUID.randomUUID().toString();
        tokenDAO.save(new PasswordResetToken(rawToken, email));

        String resetLink = frontendUrl + "/auth/reset-password?token=" + rawToken;
        emailService.sendHtml(email,
                "পাসওয়ার্ড রিসেট — E-Governance Municipal Portal",
                buildLinkEmailHtml(user.getName(), resetLink));
        return null;
    }

    public String validateToken(String token) {
        if (token == null || token.isBlank()) return "Invalid or missing token.";
        PasswordResetToken t = tokenDAO.findByToken(token);
        if (t == null)     return "This reset link is invalid. Please request a new one.";
        if (t.isUsed())    return "This reset link has already been used.";
        if (t.isExpired()) return "This reset link has expired. Please request a new one.";
        return null;
    }

    public String resetPassword(String token, String newPassword) {
        String err = validateToken(token);
        if (err != null) return err;
        PasswordResetToken t = tokenDAO.findByToken(token);
        AppUser user = userDAO.findByEmail(t.getEmail());
        if (user == null) return "User account not found.";
        userDAO.changePassword(user.getId(), passwordEncoder.encode(newPassword));
        tokenDAO.markUsed(token);
        return null;
    }

    // ── Email HTML builders ──────────────────────────────────────────────────

    private String buildOtpEmailHtml(String name, String otp) {
        return """
            <!DOCTYPE html>
            <html lang="bn">
            <head><meta charset="UTF-8"></head>
            <body style="margin:0;padding:0;background:#f3f4f6;font-family:'Segoe UI',Arial,sans-serif;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 0;background:#f3f4f6;">
                <tr><td align="center">
                  <table width="500" cellpadding="0" cellspacing="0"
                    style="background:#fff;border-radius:16px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.08);">

                    <tr>
                      <td style="background:linear-gradient(135deg,#0d5c3a,#1b7f4b);padding:30px 40px;text-align:center;">
                        <p style="margin:0;font-size:20px;font-weight:700;color:#fff;">🏛️ E-Governance Municipal Portal</p>
                        <p style="margin:6px 0 0;font-size:13px;color:rgba(255,255,255,.8);">পাসওয়ার্ড রিসেট OTP</p>
                      </td>
                    </tr>

                    <tr>
                      <td style="padding:36px 40px;">
                        <p style="margin:0 0 16px;font-size:16px;font-weight:600;color:#111827;">প্রিয় %s,</p>
                        <p style="margin:0 0 24px;font-size:14px;color:#6b7280;line-height:1.7;">
                          আপনার পাসওয়ার্ড রিসেটের জন্য নিচের <strong>6-digit OTP</strong> ব্যবহার করুন।
                          এই OTP <strong>১০ মিনিট</strong> পর্যন্ত কার্যকর থাকবে।
                        </p>

                        <!-- OTP Box -->
                        <table width="100%%" cellpadding="0" cellspacing="0">
                          <tr><td align="center" style="padding:8px 0 28px;">
                            <div style="display:inline-block;background:#f0fdf4;border:2px dashed #10b981;
                                        border-radius:12px;padding:20px 48px;">
                              <span style="font-size:42px;font-weight:800;color:#065f46;
                                           letter-spacing:12px;font-family:monospace;">%s</span>
                            </div>
                          </td></tr>
                        </table>

                        <div style="background:#fef3cd;border-left:4px solid #f59e0b;border-radius:8px;padding:14px 16px;">
                          <p style="margin:0;font-size:13px;color:#92400e;">
                            ⚠️ এই OTP কারো সাথে শেয়ার করবেন না। যদি আপনি এই অনুরোধ না করে থাকেন, এই ইমেইলটি উপেক্ষা করুন।
                          </p>
                        </div>
                      </td>
                    </tr>

                    <tr>
                      <td style="background:#f9fafb;padding:20px 40px;text-align:center;border-top:1px solid #e5e7eb;">
                        <p style="margin:0;font-size:12px;color:#9ca3af;">
                          এই ইমেইলটি স্বয়ংক্রিয়ভাবে পাঠানো হয়েছে — উত্তর দেবেন না।
                        </p>
                      </td>
                    </tr>

                  </table>
                </td></tr>
              </table>
            </body>
            </html>
            """.formatted(name, otp);
    }

    private String buildLinkEmailHtml(String name, String resetLink) {
        return """
            <!DOCTYPE html><html><body style="font-family:Arial,sans-serif;background:#f3f4f6;padding:40px 0;">
            <table width="500" align="center" style="background:#fff;border-radius:16px;overflow:hidden;">
              <tr><td style="background:#0d5c3a;padding:28px;text-align:center;">
                <p style="margin:0;color:#fff;font-size:18px;font-weight:700;">🏛️ E-Governance Portal</p>
              </td></tr>
              <tr><td style="padding:32px;">
                <p style="color:#111827;">প্রিয় %s,</p>
                <p style="color:#6b7280;">নিচের বাটনে ক্লিক করে পাসওয়ার্ড রিসেট করুন (৩০ মিনিট কার্যকর):</p>
                <p style="text-align:center;">
                  <a href="%s" style="background:#0d5c3a;color:#fff;padding:12px 32px;border-radius:8px;text-decoration:none;font-weight:600;">
                    পাসওয়ার্ড রিসেট করুন
                  </a>
                </p>
                <p style="color:#9ca3af;font-size:12px;word-break:break-all;">%s</p>
              </td></tr>
            </table>
            </body></html>
            """.formatted(name, resetLink, resetLink);
    }
}
