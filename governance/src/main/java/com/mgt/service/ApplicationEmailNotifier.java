package com.mgt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Central email notification service for all citizen applications.
 *
 * চারটি ঘটনায় email পাঠায়:
 *  1) আবেদন জমা হলে        → sendApplicationReceived(...)
 *  2) Approve/Reject হলে   → sendStatusUpdate(...)          [generic]
 *  3) Card Approved হলে    → sendCardApprovedWithPickupInfo(...)
 *     ➜ 7 working days-এর মধ্যে office/dealer counter থেকে সংগ্রহ করুন
 *  4) Scan/Distribution হলে→ sendDistributionConfirmation(...)
 *     ➜ কার্ড scan হলে citizen-কে confirmation email
 */
@Service
public class ApplicationEmailNotifier {

    @Autowired
    private EmailService emailService;

    // ─────────────────────────────────────────────
    // 1. আবেদন জমার notification
    // ─────────────────────────────────────────────
    @Async
    public void sendApplicationReceived(String email,
                                        String applicantName,
                                        String serviceName,
                                        String referenceNo) {
        if (!isValidEmail(email)) return;

        String subject = "আবেদন গৃহীত হয়েছে – " + serviceName;
        String body = buildHtml(
                "আবেদন সফলভাবে জমা হয়েছে",
                applicantName, serviceName, referenceNo, "Pending", null,
                "আপনার আবেদন সফলভাবে গৃহীত হয়েছে এবং প্রক্রিয়াধীন রয়েছে। "
              + "আবেদনের অগ্রগতি সম্পর্কে আপনাকে পরবর্তীতে জানানো হবে।"
        );
        emailService.sendHtml(email, subject, body);
    }

    // ─────────────────────────────────────────────
    // 2. Generic Approve / Reject notification
    // ─────────────────────────────────────────────
    @Async
    public void sendStatusUpdate(String email,
                                 String applicantName,
                                 String serviceName,
                                 String referenceNo,
                                 String status,
                                 String rejectionReason) {
        if (!isValidEmail(email)) return;

        boolean approved = "Approved".equalsIgnoreCase(status);
        String subject = approved
                ? "আবেদন অনুমোদিত হয়েছে – " + serviceName
                : "আবেদন বাতিল হয়েছে – " + serviceName;

        String message = approved
                ? "অভিনন্দন! আপনার আবেদন অনুমোদিত হয়েছে। "
                + "সংশ্লিষ্ট অফিস থেকে আপনার সনদ/কার্ড সংগ্রহ করুন।"
                : "দুঃখিত, আপনার আবেদনটি বাতিল করা হয়েছে।"
                + (rejectionReason != null && !rejectionReason.isBlank()
                        ? "\n\nবাতিলের কারণ: " + rejectionReason : "");

        String body = buildHtml(
                approved ? "আবেদন অনুমোদিত" : "আবেদন বাতিল",
                applicantName, serviceName, referenceNo, status, rejectionReason, message
        );
        emailService.sendHtml(email, subject, body);
    }

    // ─────────────────────────────────────────────
    // 3. Card Approved — pickup notice (7 working days)
    // ─────────────────────────────────────────────
    /**
     * Admin কার্ড approve করলে citizen-কে এই email যায়।
     * পরবর্তী ৭ কর্মদিবসের মধ্যে অফিস কাউন্টার / ডিলার থেকে কার্ড সংগ্রহের নির্দেশ।
     *
     * @param email         citizen email
     * @param holderName    কার্ডধারীর নাম
     * @param serviceName   সেবার নাম (যেমন "পারিবারিক কার্ড")
     * @param cardNo        কার্ড নম্বর
     * @param dealerName    ডিলার / অফিস কাউন্টারের নাম (null হলে generic message)
     * @param dealerContact ডিলারের যোগাযোগ নম্বর (null allowed)
     */
    @Async
    public void sendCardApprovedWithPickupInfo(String email,
                                               String holderName,
                                               String serviceName,
                                               String cardNo,
                                               String dealerName,
                                               String dealerContact) {
        if (!isValidEmail(email)) return;

        // পরবর্তী ৭ কর্মদিবস গণনা
        LocalDate deadline = addWorkingDays(LocalDate.now(), 7);
        String deadlineStr = deadline.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        String contactInfo = "";
        if (dealerName != null && !dealerName.isBlank()) {
            contactInfo = "\n\nকাউন্টার / ডিলার: " + dealerName;
            if (dealerContact != null && !dealerContact.isBlank()) {
                contactInfo += "\nযোগাযোগ: " + dealerContact;
            }
        }

        String message =
                "অভিনন্দন! আপনার " + serviceName + " আবেদন অনুমোদিত হয়েছে।\n\n"
              + "⚠️ গুরুত্বপূর্ণ: আগামী ৭ কর্মদিবসের মধ্যে (সর্বোচ্চ " + deadlineStr + " তারিখের মধ্যে) "
              + "নিকটস্থ পৌরসভা কার্যালয়ের কাউন্টার অথবা নির্ধারিত ডিলারের কাছ থেকে "
              + "আপনার কার্ড সংগ্রহ করুন।"
              + contactInfo
              + "\n\nনির্ধারিত সময়ের মধ্যে সংগ্রহ না করলে কার্ড বাতিল হতে পারে।";

        // Extra info block for email
        String extraBlock = buildInfoRow("সংগ্রহের শেষ তারিখ", deadlineStr)
                + (dealerName != null && !dealerName.isBlank()
                    ? buildInfoRow("ডিলার / কাউন্টার", dealerName) : "")
                + (dealerContact != null && !dealerContact.isBlank()
                    ? buildInfoRow("যোগাযোগ", dealerContact) : "");

        String body = buildHtmlWithExtra(
                "🎉 কার্ড অনুমোদিত — কার্ড সংগ্রহ করুন",
                holderName, serviceName, cardNo, "Approved", null, message, extraBlock
        );

        emailService.sendHtml(email, "কার্ড অনুমোদিত — সংগ্রহ করুন | " + serviceName, body);
    }

    // ─────────────────────────────────────────────
    // 4. Distribution / Scan confirmation
    // ─────────────────────────────────────────────
    /**
     * Dealer/Officer কার্ড scan করে পণ্য/সুবিধা দিলে citizen-কে confirmation email।
     *
     * @param email       citizen email
     * @param holderName  কার্ডধারীর নাম
     * @param cardNo      কার্ড নম্বর
     * @param serviceName সেবার নাম
     * @param cycleMonth  চক্র (যেমন "2025-06")
     * @param items       পণ্যের বিবরণ, যেমন "চাল: ৫ কেজি, ডাল: ২ কেজি"
     * @param distributedBy বিতরণকারীর নাম
     */
    @Async
    public void sendDistributionConfirmation(String email,
                                             String holderName,
                                             String cardNo,
                                             String serviceName,
                                             String cycleMonth,
                                             String items,
                                             String distributedBy) {
        if (!isValidEmail(email)) return;

        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        String message =
                "আপনার " + serviceName + " কার্ডের বিপরীতে পণ্য/সুবিধা সফলভাবে বিতরণ করা হয়েছে।\n\n"
              + "📦 বিতরণের বিবরণ: " + items + "\n"
              + "📅 তারিখ: " + dateStr + "\n"
              + "🔄 চক্র: " + cycleMonth + "\n"
              + (distributedBy != null && !distributedBy.isBlank()
                    ? "👤 বিতরণকারী: " + distributedBy : "")
              + "\n\nযদি আপনি এই বিতরণ গ্রহণ না করে থাকেন, অনুগ্রহ করে অবিলম্বে কর্তৃপক্ষকে জানান।";

        String extraBlock = buildInfoRow("চক্র (মাস)", cycleMonth)
                + buildInfoRow("প্রদত্ত পণ্য/সুবিধা", items)
                + buildInfoRow("বিতরণের তারিখ", dateStr)
                + (distributedBy != null && !distributedBy.isBlank()
                    ? buildInfoRow("বিতরণকারী", distributedBy) : "");

        String body = buildHtmlWithExtra(
                "✅ বিতরণ নিশ্চিতকরণ",
                holderName, serviceName, cardNo, "Approved", null, message, extraBlock
        );

        emailService.sendHtml(email,
                serviceName + " — বিতরণ নিশ্চিতকরণ | " + cycleMonth, body);
    }

    // ─────────────────────────────────────────────
    // 5. Direct HTML (e-voting, custom etc.)
    // ─────────────────────────────────────────────
    @Async
    public void sendHtml(String email, String subject, String htmlBody) {
        if (!isValidEmail(email)) return;
        emailService.sendHtml(email, subject, htmlBody);
    }

    // ─────────────────────────────────────────────
    // Helper — working days calculation
    // ─────────────────────────────────────────────
    private LocalDate addWorkingDays(LocalDate start, int workingDays) {
        LocalDate date = start;
        int added = 0;
        while (added < workingDays) {
            date = date.plusDays(1);
            if (date.getDayOfWeek() != DayOfWeek.FRIDAY
                    && date.getDayOfWeek() != DayOfWeek.SATURDAY) {
                added++;
            }
        }
        return date;
    }

    // ─────────────────────────────────────────────
    // Helper — email validation
    // ─────────────────────────────────────────────
    private boolean isValidEmail(String email) {
        if (email == null) return false;
        String cleaned = email.replaceAll("[;,\\s]+$", "").trim();
        if (cleaned.isBlank()) return false;
        int at = cleaned.indexOf('@');
        if (at <= 0 || at == cleaned.length() - 1) return false;
        String domain = cleaned.substring(at + 1);
        return domain.contains(".") && !domain.startsWith(".") && !domain.endsWith(".");
    }

    // ─────────────────────────────────────────────
    // Helper — info row for email table
    // ─────────────────────────────────────────────
    private String buildInfoRow(String label, String value) {
        return "<tr>"
             + "<td style='color:#64748b;font-size:13px;padding:6px 0'>" + label + "</td>"
             + "<td style='color:#0f172a;font-weight:600;font-size:13px'>" + value + "</td>"
             + "</tr>";
    }

    // ─────────────────────────────────────────────
    // Helper — HTML template (standard)
    // ─────────────────────────────────────────────
    private String buildHtml(String heading, String applicantName, String serviceName,
                             String referenceNo, String status, String rejectionReason,
                             String message) {
        return buildHtmlWithExtra(heading, applicantName, serviceName,
                referenceNo, status, rejectionReason, message, "");
    }

    // ─────────────────────────────────────────────
    // Helper — HTML template (with extra rows)
    // ─────────────────────────────────────────────
    private String buildHtmlWithExtra(String heading, String applicantName, String serviceName,
                                      String referenceNo, String status, String rejectionReason,
                                      String message, String extraTableRows) {

        String statusColor  = "Approved".equalsIgnoreCase(status) ? "#059669" : "#dc2626";
        String statusBg     = "Approved".equalsIgnoreCase(status) ? "#f0fdf4" : "#fef2f2";
        String statusBorder = "Approved".equalsIgnoreCase(status) ? "#059669" : "#dc2626";
        String statusLabel  = "Approved".equalsIgnoreCase(status) ? "✅ অনুমোদিত"
                            : "Pending".equalsIgnoreCase(status)   ? "⏳ প্রক্রিয়াধীন"
                            : "❌ বাতিল";

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>")
          .append("<html><head><meta charset='UTF-8'>")
          .append("<meta name='viewport' content='width=device-width,initial-scale=1'>")
          .append("</head>")
          .append("<body style='font-family:\"Segoe UI\",Arial,sans-serif;background:#f1f5f9;margin:0;padding:20px'>")

          // Card wrapper
          .append("<div style='max-width:600px;margin:0 auto;background:#fff;")
          .append("border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1)'>")

          // Header
          .append("<div style='background:linear-gradient(135deg,#064e3b,#065f46);")
          .append("padding:28px;border-bottom:4px solid #f59e0b'>")
          .append("<h2 style='color:#fff;margin:0;font-size:20px'>")
          .append("গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</h2>")
          .append("<p style='color:#a7f3d0;margin:6px 0 0;font-size:13px'>")
          .append("E-Governance Municipal Portal</p>")
          .append("</div>")

          // Body
          .append("<div style='padding:32px'>")
          .append("<h3 style='color:#0f172a;margin-top:0'>").append(heading).append("</h3>")
          .append("<p style='color:#374151;font-size:15px'>")
          .append("প্রিয় <strong>").append(applicantName).append("</strong>,</p>")
          .append("<p style='color:#374151;line-height:1.8;white-space:pre-line'>")
          .append(message).append("</p>")

          // Info box
          .append("<div style='background:#f8fafc;border:1px solid #e2e8f0;")
          .append("border-radius:8px;padding:16px 20px;margin:20px 0'>")
          .append("<table style='width:100%;border-collapse:collapse'>")
          .append(buildInfoRow("সেবার ধরন", serviceName))
          .append(buildInfoRow("আবেদন/কার্ড নম্বর", referenceNo != null ? referenceNo : "N/A"))
          .append("<tr><td style='color:#64748b;font-size:13px;padding:6px 0'>অবস্থা</td>")
          .append("<td><span style='background:").append(statusBg).append(";color:").append(statusColor)
          .append(";border:1px solid ").append(statusBorder)
          .append(";padding:2px 10px;border-radius:20px;font-size:12px;font-weight:600'>")
          .append(statusLabel).append("</span></td></tr>")
          .append(extraTableRows != null ? extraTableRows : "")
          .append("</table></div>");

        // Rejection reason box
        if (rejectionReason != null && !rejectionReason.isBlank()) {
            sb.append("<div style='background:#fef2f2;border-left:4px solid #dc2626;")
              .append("border-radius:4px;padding:12px 16px;margin-top:12px'>")
              .append("<p style='color:#7f1d1d;font-size:13px;margin:0'>")
              .append("<strong>বাতিলের কারণ:</strong> ").append(rejectionReason).append("</p>")
              .append("</div>");
        }

        sb.append("</div>")

          // Footer
          .append("<div style='background:#f8fafc;padding:16px 28px;")
          .append("border-top:1px solid #e2e8f0;text-align:center'>")
          .append("<p style='color:#94a3b8;font-size:11px;margin:0'>")
          .append("এটি একটি স্বয়ংক্রিয় বার্তা। এই email-এ reply করবেন না।<br>")
          .append("E-Governance Municipal Portal | গণপ্রজাতন্ত্রী বাংলাদেশ সরকার")
          .append("</p></div>")

          .append("</div>")
          .append("</body></html>");

        return sb.toString();
    }
}
