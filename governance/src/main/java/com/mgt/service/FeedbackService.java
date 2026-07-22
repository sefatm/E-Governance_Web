package com.mgt.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.CitizenFeedbackDAO;
import com.mgt.model.CitizenFeedback;

@Service
public class FeedbackService {

    @Autowired
    CitizenFeedbackDAO feedbackDAO;

    @Autowired
    ApplicationEmailNotifier emailNotifier;

    public CitizenFeedback submit(CitizenFeedback fb) {

        if (fb.getCitizenName() == null || fb.getCitizenName().isBlank())
            throw new RuntimeException("Name is required.");
        if (fb.getMobile() == null || fb.getMobile().isBlank())
            throw new RuntimeException("Mobile number is required.");
        if (fb.getSubject() == null || fb.getSubject().isBlank())
            throw new RuntimeException("Subject is required.");
        if (fb.getMessage() == null || fb.getMessage().isBlank())
            throw new RuntimeException("Message is required.");

        fb.setStatus("Pending");
        fb.setCreatedAt(LocalDateTime.now());
        CitizenFeedback saved = feedbackDAO.save(fb);

        // আবেদন জমার confirmation email
        if (saved.getEmail() != null && !saved.getEmail().isBlank()) {
            emailNotifier.sendApplicationReceived(
                saved.getEmail(),
                saved.getCitizenName(),
                "নাগরিক মতামত / অভিযোগ",
                "FB-" + saved.getId()
            );
        }

        return saved;
    }

    public CitizenFeedback reply(int id, String replyText, String status) {
        CitizenFeedback fb = feedbackDAO.getById(id);
        if (fb == null) throw new RuntimeException("Feedback not found.");
        if (replyText == null || replyText.isBlank()) {
            throw new RuntimeException("Reply message is required.");
        }

        fb.setAdminReply(replyText.trim());
        fb.setStatus(status != null ? status : "Resolved");
        fb.setRepliedAt(LocalDateTime.now());
        CitizenFeedback updated = feedbackDAO.update(fb);

        // Admin reply এর পর citizen কে email
        if (updated.getEmail() != null && !updated.getEmail().isBlank()) {
            String subject = "আপনার মতামতের জবাব পাওয়া গেছে – নাগরিক মতামত পোর্টাল";
            String body = buildReplyEmailHtml(
                updated.getCitizenName(),
                updated.getSubject(),
                "FB-" + updated.getId(),
                replyText,
                updated.getStatus()
            );
            emailNotifier.sendHtml(updated.getEmail(), subject, body);
        }

        return updated;
    }

    public CitizenFeedback updateStatus(int id, String status) {
        CitizenFeedback fb = feedbackDAO.getById(id);
        if (fb == null) throw new RuntimeException("Feedback not found.");
        fb.setStatus(status);
        CitizenFeedback updated = feedbackDAO.update(fb);

        // Status update email
        if (updated.getEmail() != null && !updated.getEmail().isBlank()) {
            emailNotifier.sendStatusUpdate(
                updated.getEmail(),
                updated.getCitizenName(),
                "নাগরিক মতামত / অভিযোগ",
                "FB-" + updated.getId(),
                status,
                null
            );
        }

        return updated;
    }

    public List<CitizenFeedback> getAll()                        { return feedbackDAO.getAll(); }
    public CitizenFeedback       getById(int id)                 { return feedbackDAO.getById(id); }
    public List<CitizenFeedback> getByStatus(String status)      { return feedbackDAO.getByStatus(status); }
    public List<CitizenFeedback> getByCategory(String category)  { return feedbackDAO.getByCategory(category); }
    public void                  delete(int id)                  { feedbackDAO.delete(id); }

    public Map<String, Object> getSummary() {
        Map<String, Object> map = new HashMap<>();
        map.put("total",       feedbackDAO.getAll().size());
        map.put("pending",     feedbackDAO.countByStatus("Pending"));
        map.put("underReview", feedbackDAO.countByStatus("UnderReview"));
        map.put("resolved",    feedbackDAO.countByStatus("Resolved"));
        map.put("closed",      feedbackDAO.countByStatus("Closed"));
        map.put("avgRating",   feedbackDAO.avgRating());
        return map;
    }

    // ─────────────────────────────────────────────
    // HTML template for admin reply email
    // ─────────────────────────────────────────────
    private String buildReplyEmailHtml(String citizenName, String subject,
                                        String refNo, String replyText, String status) {
        String statusColor = "Resolved".equalsIgnoreCase(status) ? "#059669" : "#d97706";
        String statusLabel = "Resolved".equalsIgnoreCase(status) ? "✅ সমাধান হয়েছে" : "⏳ প্রক্রিয়াধীন";

        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
            + "<body style='font-family:\"Segoe UI\",Arial,sans-serif;background:#f1f5f9;margin:0;padding:20px'>"
            + "<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1)'>"
            + "<div style='background:linear-gradient(135deg,#064e3b,#065f46);padding:28px;border-bottom:4px solid #f59e0b'>"
            + "<h2 style='color:#fff;margin:0;font-size:20px'>গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</h2>"
            + "<p style='color:#a7f3d0;margin:6px 0 0;font-size:13px'>E-Governance Municipal Portal</p>"
            + "</div>"
            + "<div style='padding:32px'>"
            + "<h3 style='color:#0f172a;margin-top:0'>আপনার মতামতের জবাব এসেছে</h3>"
            + "<p style='color:#374151;font-size:15px'>প্রিয় <strong>" + citizenName + "</strong>,</p>"
            + "<p style='color:#374151'>আপনার \"<strong>" + subject + "</strong>\" বিষয়ক মতামতের জবাব দেওয়া হয়েছে।</p>"
            + "<div style='background:#f0fdf4;border:1px solid #bbf7d0;border-radius:8px;padding:16px 20px;margin:20px 0'>"
            + "<p style='color:#064e3b;font-size:13px;font-weight:600;margin:0 0 8px'>কর্তৃপক্ষের জবাব:</p>"
            + "<p style='color:#0f172a;font-size:14px;line-height:1.7;margin:0'>" + replyText + "</p>"
            + "</div>"
            + "<div style='background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:16px 20px;margin:16px 0'>"
            + "<table style='width:100%;border-collapse:collapse'>"
            + "<tr><td style='color:#64748b;font-size:13px;padding:6px 0'>রেফারেন্স নম্বর</td>"
            + "<td style='color:#0f172a;font-weight:600;font-size:13px'>" + refNo + "</td></tr>"
            + "<tr><td style='color:#64748b;font-size:13px;padding:6px 0'>অবস্থা</td>"
            + "<td><span style='color:" + statusColor + ";font-weight:600;font-size:13px'>" + statusLabel + "</span></td></tr>"
            + "</table></div>"
            + "</div>"
            + "<div style='background:#f8fafc;padding:16px 28px;border-top:1px solid #e2e8f0;text-align:center'>"
            + "<p style='color:#94a3b8;font-size:11px;margin:0'>এটি একটি স্বয়ংক্রিয় বার্তা। এই email-এ reply করবেন না।<br>"
            + "E-Governance Municipal Portal | গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</p>"
            + "</div></div></body></html>";
    }
}
