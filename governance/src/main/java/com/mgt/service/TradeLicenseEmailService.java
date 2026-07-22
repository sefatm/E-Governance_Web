package com.mgt.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mgt.model.BusinessCategory;
import com.mgt.model.TradeLicenseApply;

/**
 * TradeLicenseEmailService — Trade License এর সব email এখানে।
 *
 * ⚠️ কেন আলাদা Service?
 * Spring @Async শুধু তখনই কাজ করে যখন method call অন্য bean থেকে আসে।
 * Same class এর private method → @Async কাজ করে না (proxy bypass)।
 * তাই email logic এই আলাদা @Service এ রাখা হয়েছে।
 */
@Service
public class TradeLicenseEmailService {

    @Autowired private EmailService emailService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // ── 1. আবেদন জমার email ────────────────────────────────────────────────
    @Async("taskExecutor")
    public void sendApplyReceived(TradeLicenseApply t) {
        if (!hasEmail(t.getEmail())) return;
        try {
            BusinessCategory cat = BusinessCategory.getByName(t.getBusinessType());
            String subject = "⏳ Trade License আবেদন গৃহীত হয়েছে — " + t.getLicenseNumber();
            String html = header("#92400e", "#fef3c7", "⏳ প্রক্রিয়াধীন")
                + body("Trade License আবেদন গৃহীত হয়েছে",
                       t.getOwnerName(),
                       "আপনার Trade License আবেদন সফলভাবে গৃহীত হয়েছে এবং পর্যালোচনাধীন রয়েছে। অনুমোদন বা বাতিলের বিষয়ে আপনাকে email-এ জানানো হবে।",
                       infoTable(
                           row("License নম্বর",     t.getLicenseNumber()),
                           row("Business নাম",      t.getBusinessName()),
                           row("Business Category", cat != null ? cat.getNameBn() : t.getBusinessType()),
                           row("আবেদনের তারিখ",    LocalDate.now().format(DATE_FMT))
                       ))
                + footer();
            emailService.sendHtml(t.getEmail(), subject, html);
        } catch (Exception e) {
            System.err.println("[TradeLicenseEmailService] Apply email error: " + e.getMessage());
        }
    }

    // ── 2. Approved email ───────────────────────────────────────────────────
    @Async("taskExecutor")
    public void sendApproved(TradeLicenseApply t, LocalDate expiry) {
        if (!hasEmail(t.getEmail())) return;
        try {
            BusinessCategory cat = BusinessCategory.getByName(t.getBusinessType());
            String subject = "✅ Trade License অনুমোদিত — " + t.getLicenseNumber();
            String html = header("#15803d", "#dcfce7", "✅ অনুমোদিত")
                + body("Trade License অনুমোদিত হয়েছে!",
                       t.getOwnerName(),
                       "অভিনন্দন! আপনার Trade License আবেদন সফলভাবে অনুমোদিত হয়েছে। পোর্টালে লগইন করে Trade License সনদ ডাউনলোড করুন।",
                       infoTable(
                           row("License নম্বর",     t.getLicenseNumber()),
                           row("Business নাম",      t.getBusinessName()),
                           row("Business Category", cat != null ? cat.getNameBn() : t.getBusinessType()),
                           row("অনুমোদনের তারিখ",  LocalDate.now().format(DATE_FMT)),
                           row("Expiry তারিখ",
                               "<span style=\"color:#16a34a;font-weight:700\">"
                               + expiry.format(DATE_FMT) + "</span>")
                       ))
                + footer();
            emailService.sendHtml(t.getEmail(), subject, html);
        } catch (Exception e) {
            System.err.println("[TradeLicenseEmailService] Approved email error: " + e.getMessage());
        }
    }

    // ── 3. Rejected email ───────────────────────────────────────────────────
    @Async("taskExecutor")
    public void sendRejected(TradeLicenseApply t) {
        if (!hasEmail(t.getEmail())) return;
        try {
            String subject = "❌ Trade License আবেদন বাতিল — " + t.getLicenseNumber();
            String html = header("#b91c1c", "#fee2e2", "❌ বাতিল")
                + "<div style=\"padding:28px 32px\">"
                + "<h3 style=\"color:#0f172a;margin:0 0 8px\">Trade License আবেদন বাতিল হয়েছে</h3>"
                + "<p style=\"color:#374151;margin:0 0 16px\">প্রিয় <strong>" + t.getOwnerName() + "</strong>,<br><br>"
                + "দুঃখজনকভাবে আপনার Trade License আবেদন (<strong>" + t.getLicenseNumber() + "</strong>) বাতিল করা হয়েছে।</p>"
                + "<div style=\"background:#fef2f2;border-left:4px solid #dc2626;border-radius:6px;"
                + "padding:12px 16px;color:#7f1d1d;font-size:13px;margin-bottom:20px\">"
                + "সঠিক তথ্য ও প্রয়োজনীয় কাগজপত্র নিয়ে পুনরায় আবেদন করুন। আরও তথ্যের জন্য পৌরসভা অফিসে যোগাযোগ করুন."
                + "</div></div>"
                + footer();
            emailService.sendHtml(t.getEmail(), subject, html);
        } catch (Exception e) {
            System.err.println("[TradeLicenseEmailService] Rejected email error: " + e.getMessage());
        }
    }

    // ── 4. Renewal Reminder email ───────────────────────────────────────────
    @Async("taskExecutor")
    public void sendRenewalReminder(TradeLicenseApply t, int daysLeft) {
        if (!hasEmail(t.getEmail())) return;
        try {
            boolean urgent    = daysLeft <= 30;
            String  subject   = (urgent ? "🔴 জরুরি" : "🟡 সতর্কতা")
                              + ": Trade License " + daysLeft + " দিনে expire — " + t.getLicenseNumber();
            String  badgeBg   = urgent ? "#fee2e2" : "#fef3c7";
            String  badgeClr  = urgent ? "#b91c1c" : "#92400e";
            String  badgeTxt  = (urgent ? "🔴 " : "🟡 ") + daysLeft + " দিন বাকি";

            String html = header(badgeClr, badgeBg, badgeTxt)
                + body("Trade License Renewal করুন!",
                       t.getOwnerName(),
                       "আপনার Trade License মাত্র <strong>" + daysLeft + " দিন</strong> পরে মেয়াদোত্তীর্ণ হবে। দেরি না করে এখনই Renewal করুন।",
                       infoTable(
                           row("License নম্বর", t.getLicenseNumber()),
                           row("Business নাম",  t.getBusinessName()),
                           row("Expiry তারিখ",
                               "<span style=\"color:#dc2626;font-weight:700\">"
                               + (t.getExpiryDate() != null ? t.getExpiryDate().format(DATE_FMT) : "—")
                               + "</span>")
                       )
                       + "<div style=\"background:#fffbeb;border-left:4px solid #f59e0b;border-radius:6px;"
                       + "padding:12px 16px;color:#92400e;font-size:13px;margin-top:12px\">"
                       + "⚠️ সময়মতো Renewal না করলে মাসিক ৫% হারে Late Fine প্রযোজ্য হবে।</div>")
                + footer();
            emailService.sendHtml(t.getEmail(), subject, html);
        } catch (Exception e) {
            System.err.println("[TradeLicenseEmailService] Reminder email error: " + e.getMessage());
        }
    }

    // ── Template helpers ────────────────────────────────────────────────────

    private String header(String badgeColor, String badgeBg, String badgeText) {
        return "<!DOCTYPE html><html lang=\"bn\"><head><meta charset=\"UTF-8\">"
             + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"></head>"
             + "<body style=\"font-family:'Segoe UI',Arial,sans-serif;background:#f1f5f9;margin:0;padding:20px\">"
             + "<div style=\"max-width:600px;margin:0 auto;background:#fff;border-radius:12px;"
             + "overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1)\">"
             + "<div style=\"background:linear-gradient(135deg,#064e3b,#065f46);"
             + "padding:24px 28px;border-bottom:4px solid #f59e0b\">"
             + "<table style=\"width:100%;border-collapse:collapse\"><tr>"
             + "<td><p style=\"color:#a7f3d0;font-size:12px;margin:0 0 2px\">গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</p>"
             + "<h2 style=\"color:#fff;margin:0;font-size:18px;font-weight:700\">E-Governance Municipal Portal</h2>"
             + "<p style=\"color:#6ee7b7;font-size:11px;margin:4px 0 0\">Government of the People's Republic of Bangladesh</p></td>"
             + "<td style=\"text-align:right;vertical-align:middle\">"
             + "<span style=\"background:" + badgeBg + ";color:" + badgeColor + ";padding:5px 14px;"
             + "border-radius:20px;font-size:13px;font-weight:600\">" + badgeText + "</span>"
             + "</td></tr></table></div>";
    }

    private String body(String heading, String name, String message, String content) {
        return "<div style=\"padding:28px 32px\">"
             + "<h3 style=\"color:#0f172a;margin:0 0 8px\">" + heading + "</h3>"
             + "<p style=\"color:#374151;font-size:15px\">প্রিয় <strong>" + name + "</strong>,</p>"
             + "<p style=\"color:#374151;line-height:1.8\">" + message + "</p>"
             + content
             + "</div>";
    }

    private String infoTable(String... rows) {
        StringBuilder sb = new StringBuilder(
            "<div style=\"background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;"
          + "padding:4px 16px;margin:16px 0\"><table style=\"width:100%;border-collapse:collapse\">");
        for (String r : rows) sb.append(r);
        sb.append("</table></div>");
        return sb.toString();
    }

    private String row(String label, String value) {
        return "<tr>"
             + "<td style=\"color:#64748b;font-size:13px;padding:8px 0;"
             + "border-bottom:1px solid #f1f5f9;white-space:nowrap\">" + label + "</td>"
             + "<td style=\"color:#0f172a;font-weight:600;font-size:13px;"
             + "padding:8px 0 8px 12px;border-bottom:1px solid #f1f5f9\">" + value + "</td>"
             + "</tr>";
    }

    private String footer() {
        return "<div style=\"background:#f8fafc;padding:14px 28px;border-top:1px solid #e2e8f0;text-align:center\">"
             + "<p style=\"color:#94a3b8;font-size:11px;margin:0\">"
             + "এটি একটি স্বয়ংক্রিয় বার্তা। এই email-এ reply করবেন না।<br>"
             + "E-Governance Municipal Portal | গণপ্রজাতন্ত্রী বাংলাদেশ সরকার"
             + "</p></div></div></body></html>";
    }

    private boolean hasEmail(String email) {
        if (email == null || email.isBlank()) return false;
        String c = email.replaceAll("[;,\\s]+$", "").trim();
        int at = c.indexOf('@');
        if (at <= 0 || at == c.length() - 1) return false;
        String domain = c.substring(at + 1);
        return domain.contains(".") && !domain.startsWith(".") && !domain.endsWith(".");
    }
}
