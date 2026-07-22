package com.mgt.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.TradeLicenseDAO;
import com.mgt.dao.TradeInspectionDAO;
import com.mgt.model.TradeLicenseApply;
import com.mgt.model.TradeInspection;

/**
 * TradeInspectionService
 *
 * Workflow:
 *   1. Admin → scheduleInspection()   → Inspection তৈরি, applicant কে email
 *   2. Inspector → completeInspection() → Outcome লেখে (Passed/Failed)
 *      → Passed হলে license Approved, email যায়
 *      → Failed হলে license Rejected, email যায়
 *   3. Admin → cancelInspection()     → Cancel করতে পারে
 */
@Service
public class TradeInspectionService {

    @Autowired private TradeInspectionDAO inspectionDAO;
    @Autowired private TradeLicenseDAO    licenseDAO;
    @Autowired private EmailService       emailService;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    // ─── Schedule Inspection ─────────────────────────────────────────────────
    /**
     * Admin inspection schedule করবে
     * Applicant কে email notification যাবে
     */
    public TradeInspection scheduleInspection(TradeInspection inspection) {
        // License exist করে কিনা check
        TradeLicenseApply license = licenseDAO.getById(inspection.getLicenseId());
        if (license == null)
            throw new RuntimeException("Trade License Application পাওয়া যায়নি।");

        // Auto-fill license info
        inspection.setLicenseNumber(license.getLicenseNumber());
        inspection.setBusinessName(license.getBusinessName());
        inspection.setBusinessAddress(license.getAddress());
        inspection.setApplicantName(license.getOwnerName());
        inspection.setApplicantEmail(license.getEmail());
        inspection.setStatus("Scheduled");
        inspection.setScheduledAt(LocalDateTime.now());

        TradeInspection saved = inspectionDAO.save(inspection);

        // Applicant কে email পাঠাও
        if (license.getEmail() != null && !license.getEmail().isBlank()) {
            sendScheduledEmail(saved);
        }

        return saved;
    }

    // ─── Complete Inspection ─────────────────────────────────────────────────
    /**
     * Inspector inspection complete করবে — outcome লিখবে
     * Outcome অনুযায়ী license auto Approved / Rejected হবে
     *
     * @param id        Inspection ID
     * @param outcome   "Passed" অথবা "Failed"
     * @param remarks   Inspector এর বিস্তারিত note
     */
    public TradeInspection completeInspection(int id, String outcome, String remarks) {
        TradeInspection inspection = inspectionDAO.getById(id);
        if (inspection == null)
            throw new RuntimeException("Inspection পাওয়া যায়নি।");
        if ("Completed".equals(inspection.getStatus()))
            throw new RuntimeException("এই Inspection ইতোমধ্যে Complete হয়েছে।");
        if ("Cancelled".equals(inspection.getStatus()))
            throw new RuntimeException("Cancelled Inspection complete করা যাবে না।");

        inspection.setOutcome(outcome);
        inspection.setRemarks(remarks);
        inspection.setStatus("Completed");
        inspection.setCompletedAt(LocalDateTime.now());
        TradeInspection updated = inspectionDAO.update(inspection);

        // License auto update
        TradeLicenseApply license = licenseDAO.getById(inspection.getLicenseId());
        if (license != null) {
            if ("Passed".equalsIgnoreCase(outcome)) {
                // License Approve করো + expiry set করো
                java.time.LocalDate expiry = java.time.LocalDate.now()
                    .plusYears(license.getLicensePeriod() != null ? license.getLicensePeriod() : 1);
                licenseDAO.updateStatus(license.getId(), "Approved");
                licenseDAO.setExpiryDate(license.getId(), expiry);
                sendOutcomeEmail(updated, license, true);
            } else {
                // License Reject করো
                licenseDAO.updateStatus(license.getId(), "Rejected");
                sendOutcomeEmail(updated, license, false);
            }
        }

        return updated;
    }

    // ─── Cancel Inspection ────────────────────────────────────────────────────
    public TradeInspection cancelInspection(int id, String reason) {
        TradeInspection inspection = inspectionDAO.getById(id);
        if (inspection == null) throw new RuntimeException("Inspection পাওয়া যায়নি।");
        inspection.setStatus("Cancelled");
        inspection.setRemarks(reason);
        return inspectionDAO.update(inspection);
    }

    // ─── Queries ──────────────────────────────────────────────────────────────
    public List<TradeInspection> getAll()                        { return inspectionDAO.getAll(); }
    public TradeInspection getById(int id)                       { return inspectionDAO.getById(id); }
    public List<TradeInspection> getByLicenseId(int licenseId)  { return inspectionDAO.getByLicenseId(licenseId); }
    public List<TradeInspection> getByStatus(String status)     { return inspectionDAO.getByStatus(status); }
    public List<TradeInspection> getTodaysInspections()         { return inspectionDAO.getTodaysInspections(); }

    // ─── Email Templates ─────────────────────────────────────────────────────

    private void sendScheduledEmail(TradeInspection ins) {
        String subject = "📋 Physical Inspection Schedule — " + ins.getLicenseNumber();
        String html = """
            <!DOCTYPE html><html lang="bn"><head><meta charset="UTF-8">
            <style>
              body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px}
              .card{background:#fff;border-radius:10px;max-width:580px;margin:auto;
                    padding:32px;border-top:5px solid #2563eb}
              .badge{background:#dbeafe;color:#1d4ed8;padding:6px 16px;border-radius:20px;
                     font-size:14px;display:inline-block;margin-bottom:16px}
              h2{color:#1d4ed8;margin:0 0 12px}
              p{color:#374151;line-height:1.7;margin:8px 0}
              .info-box{background:#eff6ff;border:1px solid #bfdbfe;border-radius:8px;padding:20px;margin:20px 0}
              .row{display:flex;padding:8px 0;border-bottom:1px solid #dbeafe;font-size:14px}
              .row:last-child{border-bottom:none}
              .label{color:#6b7280;min-width:160px;flex-shrink:0}
              .value{color:#111827;font-weight:600}
              .notice{background:#fef9c3;border:1px solid #fde047;border-radius:8px;
                      padding:12px 16px;margin:16px 0;color:#713f12;font-size:14px}
              .footer{text-align:center;color:#9ca3af;font-size:12px;margin-top:28px}
            </style></head>
            <body><div class="card">
              <span class="badge">📋 Inspection Scheduled</span>
              <h2>Physical Inspection Schedule করা হয়েছে</h2>
              <p>প্রিয় <strong>%s</strong>,</p>
              <p>আপনার Trade License আবেদনের জন্য Physical Inspection Schedule করা হয়েছে।
                 নিচের তারিখ ও সময়ে আমাদের Inspector আপনার Business Premises পরিদর্শন করবে।</p>

              <div class="info-box">
                <div class="row"><span class="label">License নম্বর</span><span class="value">%s</span></div>
                <div class="row"><span class="label">Business এর নাম</span><span class="value">%s</span></div>
                <div class="row"><span class="label">Inspection তারিখ</span><span class="value">%s</span></div>
                <div class="row"><span class="label">Inspection সময়</span><span class="value">%s</span></div>
                <div class="row"><span class="label">Inspector এর নাম</span><span class="value">%s</span></div>
                <div class="row"><span class="label">পরিদর্শনের ঠিকানা</span><span class="value">%s</span></div>
              </div>

              <div class="notice">
                ⚠️ অনুগ্রহ করে নির্ধারিত সময়ে Business Premises এ উপস্থিত থাকুন।
                প্রয়োজনীয় কাগজপত্র প্রস্তুত রাখুন।
              </div>

              <div class="footer">E-Governance Municipal Portal | এই ইমেইলে Reply করবেন না</div>
            </div></body></html>
            """.formatted(
                ins.getApplicantName(),
                ins.getLicenseNumber(),
                ins.getBusinessName(),
                ins.getInspectionDate() != null ? ins.getInspectionDate().format(DATE_FMT) : "—",
                ins.getInspectionTime() != null ? ins.getInspectionTime().format(TIME_FMT) : "—",
                ins.getInspectorName() != null ? ins.getInspectorName() : "পৌরসভার Inspector",
                ins.getBusinessAddress()
            );
        emailService.sendHtml(ins.getApplicantEmail(), subject, html);
    }

    private void sendOutcomeEmail(TradeInspection ins, TradeLicenseApply license, boolean passed) {
        if (license.getEmail() == null || license.getEmail().isBlank()) return;

        String subject = passed
            ? "✅ Inspection Passed — Trade License Approved — " + license.getLicenseNumber()
            : "❌ Inspection Failed — Trade License Rejected — " + license.getLicenseNumber();

        String borderColor = passed ? "#16a34a" : "#dc2626";
        String badgeBg     = passed ? "#dcfce7" : "#fee2e2";
        String badgeColor  = passed ? "#15803d" : "#b91c1c";
        String headColor   = passed ? "#15803d" : "#b91c1c";
        String badgeText   = passed ? "✅ Inspection Passed" : "❌ Inspection Failed";
        String headline    = passed ? "Trade License Approved!" : "Trade License Rejected";
        String body        = passed
            ? "আপনার Business Premises সফলভাবে Inspection পাস করেছে। আপনার Trade License অনুমোদিত হয়েছে।"
            : "দুঃখজনকভাবে আপনার Business Premises Inspection এ উত্তীর্ণ হয়নি। Trade License আবেদন বাতিল করা হয়েছে।";
        String note        = passed
            ? "পোর্টালে লগইন করে আপনার Trade License সনদ ডাউনলোড করুন।"
            : "সমস্যা সংশোধন করে পুনরায় আবেদন করুন।";

        String html = """
            <!DOCTYPE html><html lang="bn"><head><meta charset="UTF-8">
            <style>
              body{font-family:Arial,sans-serif;background:#f4f4f4;margin:0;padding:20px}
              .card{background:#fff;border-radius:10px;max-width:580px;margin:auto;
                    padding:32px;border-top:5px solid %s}
              .badge{background:%s;color:%s;padding:6px 16px;border-radius:20px;
                     font-size:14px;display:inline-block;margin-bottom:16px}
              h2{color:%s;margin:0 0 12px}
              p{color:#374151;line-height:1.7;margin:8px 0}
              .info-box{background:#f9fafb;border:1px solid #e5e7eb;border-radius:8px;padding:20px;margin:20px 0}
              .row{display:flex;padding:8px 0;border-bottom:1px solid #f3f4f6;font-size:14px}
              .row:last-child{border-bottom:none}
              .label{color:#6b7280;min-width:140px;flex-shrink:0}
              .value{color:#111827;font-weight:600}
              .remarks{background:#f3f4f6;border-left:4px solid %s;padding:12px 16px;
                       border-radius:4px;color:#374151;margin:16px 0;font-size:14px}
              .footer{text-align:center;color:#9ca3af;font-size:12px;margin-top:28px}
            </style></head>
            <body><div class="card">
              <span class="badge">%s</span>
              <h2>%s</h2>
              <p>প্রিয় <strong>%s</strong>,</p>
              <p>%s</p>
              <div class="info-box">
                <div class="row"><span class="label">License নম্বর</span><span class="value">%s</span></div>
                <div class="row"><span class="label">Business এর নাম</span><span class="value">%s</span></div>
                <div class="row"><span class="label">Inspection তারিখ</span><span class="value">%s</span></div>
                <div class="row"><span class="label">Inspector</span><span class="value">%s</span></div>
              </div>
              <div class="remarks"><strong>Inspector এর মন্তব্য:</strong><br>%s</div>
              <p>%s</p>
              <div class="footer">E-Governance Municipal Portal | এই ইমেইলে Reply করবেন না</div>
            </div></body></html>
            """.formatted(
                borderColor, badgeBg, badgeColor, headColor, borderColor,
                badgeText, headline,
                license.getOwnerName(), body,
                license.getLicenseNumber(), license.getBusinessName(),
                ins.getCompletedAt() != null ? ins.getCompletedAt().toLocalDate().format(DATE_FMT) : "—",
                ins.getInspectorName() != null ? ins.getInspectorName() : "—",
                ins.getRemarks() != null ? ins.getRemarks() : "—",
                note
            );
        emailService.sendHtml(license.getEmail(), subject, html);
    }
}
