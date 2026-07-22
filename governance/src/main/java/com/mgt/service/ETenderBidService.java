package com.mgt.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;   // FIX: import

import com.mgt.dao.ETenderBidDAO;
import com.mgt.dao.ETenderNoticeDAO;
import com.mgt.model.ETenderBid;
import com.mgt.model.ETenderNotice;

@Service
public class ETenderBidService {

    @Autowired private ETenderBidDAO       bidDAO;
    @Autowired private ETenderNoticeDAO    noticeDAO;
    @Autowired private VendorBlacklistService blacklistService;
    @Autowired private EmailService        emailService;

    // FIX 3: @Transactional যোগ — bid save + recalculateLowest একই transaction-এ
    @Transactional
    public ETenderBid submitBid(ETenderBid bid) {

        ETenderNotice notice = noticeDAO.getById(bid.getTenderId());
        if (notice == null)
            throw new RuntimeException("Tender পাওয়া যায়নি।");

        if (!"Open".equalsIgnoreCase(notice.getStatus()))
            throw new RuntimeException("এই Tender এ আর bid জমা নেওয়া হচ্ছে না।");

        if (notice.getEndDate().isBefore(java.time.LocalDate.now()))
            throw new RuntimeException("Bid জমার সময়সীমা শেষ হয়ে গেছে।");

        if (blacklistService.isBlacklisted(bid.getNid(), bid.getEmail(), bid.getMobile()))
            throw new RuntimeException(
                "আপনি blacklisted vendor। এই Tender এ bid জমা দেওয়ার অনুমতি নেই।");

        bid.setStatus("Submitted");
        bid.setSubmittedAt(LocalDateTime.now());
        ETenderBid saved = bidDAO.save(bid);

        // FIX 8: @Transactional থাকায় এই দুটো operation এখন atomic
        bidDAO.recalculateLowest(bid.getTenderId());

        // Bid জমার confirmation email
        if (saved.getEmail() != null && !saved.getEmail().isBlank()) {
            String subject = "✅ Bid সফলভাবে জমা হয়েছে — " + notice.getTenderNo();
            String html = buildBidReceivedHtml(saved, notice);
            emailService.sendHtml(saved.getEmail(), subject, html);
        }

        return saved;
    }

    private String buildBidReceivedHtml(ETenderBid bid, ETenderNotice notice) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
            + "<body style='font-family:Arial,sans-serif;background:#f1f5f9;padding:20px'>"
            + "<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:12px;"
            + "overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1)'>"
            + "<div style='background:linear-gradient(135deg,#1e3a5f,#1e40af);padding:28px;"
            + "border-bottom:4px solid #f59e0b'>"
            + "<h2 style='color:#fff;margin:0'>গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</h2>"
            + "<p style='color:#bfdbfe;margin:6px 0 0;font-size:13px'>E-Tender Management System</p></div>"
            + "<div style='padding:32px'>"
            + "<h3 style='color:#1e40af'>⏳ Bid সফলভাবে জমা হয়েছে</h3>"
            + "<p>প্রিয় <strong>" + bid.getBidderName() + "</strong>,</p>"
            + "<p>আপনার Bid সফলভাবে জমা হয়েছে এবং প্রক্রিয়াধীন রয়েছে।</p>"
            + "<div style='background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:16px;margin:20px 0'>"
            + "<table style='width:100%;border-collapse:collapse'>"
            + "<tr><td style='color:#64748b;font-size:13px;padding:7px 0'>Bid ID</td>"
            + "<td style='font-weight:700'>#" + bid.getId() + "</td></tr>"
            + "<tr><td style='color:#64748b;font-size:13px;padding:7px 0'>টেন্ডার নম্বর</td>"
            + "<td style='font-weight:700'>" + notice.getTenderNo() + "</td></tr>"
            + "<tr><td style='color:#64748b;font-size:13px;padding:7px 0'>কাজের শিরোনাম</td>"
            + "<td>" + notice.getTitle() + "</td></tr>"
            + "<tr><td style='color:#64748b;font-size:13px;padding:7px 0'>আপনার Bid পরিমাণ</td>"
            + "<td style='color:#059669;font-weight:700'>৳ " + String.format("%,.2f", bid.getBidAmount()) + "</td></tr>"
            + "<tr><td style='color:#64748b;font-size:13px;padding:7px 0'>কোম্পানি</td>"
            + "<td>" + bid.getCompanyName() + "</td></tr>"
            + "</table></div>"
            + "<p style='color:#6b7280;font-size:13px'>টেন্ডার ফলাফল প্রকাশিত হলে আপনাকে email করা হবে।</p>"
            + "</div><div style='background:#f8fafc;padding:16px;text-align:center;"
            + "border-top:1px solid #e2e8f0'><p style='color:#94a3b8;font-size:11px;margin:0'>"
            + "E-Tender Management System | গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</p></div>"
            + "</div></body></html>";
    }

    public void verifyDocument(int bidId, Boolean verified, String remark) {
        bidDAO.updateDocVerification(bidId, verified, remark);
        ETenderBid bid = bidDAO.getById(bidId);
        if (bid != null && bid.getEmail() != null && !bid.getEmail().isBlank())
            sendDocVerificationEmail(bid, verified, remark);
    }

    private void sendDocVerificationEmail(ETenderBid bid, Boolean verified, String remark) {
        String subject = Boolean.TRUE.equals(verified)
            ? "✅ আপনার Tender Document Verified — Bid #" + bid.getId()
            : "❌ Tender Document Rejected — Bid #" + bid.getId();
        String html = Boolean.TRUE.equals(verified)
            ? buildDocApprovedHtml(bid, remark)
            : buildDocRejectedHtml(bid, remark);
        emailService.sendHtml(bid.getEmail(), subject, html);
    }

    public ETenderBid getLowestBid(int tenderId) { return bidDAO.getLowestBid(tenderId); }
    public List<ETenderBid> getAll()              { return bidDAO.getAll(); }
    public List<ETenderBid> getByTenderId(int t)  { return bidDAO.getByTenderId(t); }
    public ETenderBid getById(int id)              { return bidDAO.getById(id); }
    public void updateStatus(int id, String s)     { bidDAO.updateStatus(id, s); }
    public long getBidCount(int tenderId)          { return bidDAO.countByTenderId(tenderId); }

    private String buildDocApprovedHtml(ETenderBid bid, String remark) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
            + "<body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:20px'>"
            + "<div style='max-width:560px;margin:auto;background:#fff;border-radius:10px;"
            + "padding:32px;border-top:5px solid #16a34a'>"
            + "<div style='background:#dcfce7;color:#15803d;padding:6px 16px;border-radius:20px;"
            + "font-size:14px;display:inline-block;margin-bottom:16px'>✅ Document Verified</div>"
            + "<h2 style='color:#15803d'>আপনার Document অনুমোদিত!</h2>"
            + "<p>প্রিয় <strong>" + bid.getBidderName() + "</strong>,</p>"
            + "<p>আপনার Bid Document সফলভাবে Verify করা হয়েছে।</p>"
            + "<table style='width:100%;background:#f0fdf4;border-radius:8px;padding:16px;border-collapse:collapse'>"
            + "<tr><td style='color:#6b7280;padding:6px'>Bid ID</td><td><strong>#" + bid.getId() + "</strong></td></tr>"
            + "<tr><td style='color:#6b7280;padding:6px'>Company</td><td><strong>" + bid.getCompanyName() + "</strong></td></tr>"
            + "<tr><td style='color:#6b7280;padding:6px'>Bid Amount</td><td><strong>৳ " + String.format("%,.2f", bid.getBidAmount()) + "</strong></td></tr>"
            + "<tr><td style='color:#6b7280;padding:6px'>Admin Note</td><td>" + (remark != null ? remark : "—") + "</td></tr>"
            + "</table>"
            + "<p style='color:#9ca3af;font-size:11px;margin-top:24px;text-align:center'>"
            + "E-Governance Municipal Portal</p></div></body></html>";
    }

    private String buildDocRejectedHtml(ETenderBid bid, String remark) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
            + "<body style='font-family:Arial,sans-serif;background:#f4f4f4;padding:20px'>"
            + "<div style='max-width:560px;margin:auto;background:#fff;border-radius:10px;"
            + "padding:32px;border-top:5px solid #dc2626'>"
            + "<div style='background:#fee2e2;color:#b91c1c;padding:6px 16px;border-radius:20px;"
            + "font-size:14px;display:inline-block;margin-bottom:16px'>❌ Document Rejected</div>"
            + "<h2 style='color:#b91c1c'>Document Verify হয়নি</h2>"
            + "<p>প্রিয় <strong>" + bid.getBidderName() + "</strong>,</p>"
            + "<table style='width:100%;background:#fef2f2;border-radius:8px;padding:16px;border-collapse:collapse'>"
            + "<tr><td style='color:#6b7280;padding:6px'>Bid ID</td><td><strong>#" + bid.getId() + "</strong></td></tr>"
            + "<tr><td style='color:#6b7280;padding:6px'>Company</td><td><strong>" + bid.getCompanyName() + "</strong></td></tr>"
            + "</table>"
            + "<div style='background:#fff5f5;border-left:4px solid #dc2626;padding:12px;margin:16px 0;border-radius:4px'>"
            + "<strong>কারণ:</strong> " + (remark != null ? remark : "Document সঠিক নয়।") + "</div>"
            + "<p>সঠিক document নিয়ে পুনরায় bid জমা দিন।</p>"
            + "<p style='color:#9ca3af;font-size:11px;margin-top:24px;text-align:center'>"
            + "E-Governance Municipal Portal</p></div></body></html>";
    }
}
