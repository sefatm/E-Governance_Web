package com.mgt.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mgt.dao.ETenderAwardDAO;
import com.mgt.dao.ETenderBidDAO;
import com.mgt.dao.ETenderNoticeDAO;
import com.mgt.model.ETenderAward;
import com.mgt.model.ETenderBid;
import com.mgt.model.ETenderNotice;
import com.mgt.model.ProjectBudget;
import com.mgt.model.ProjectList;

@Service
public class ETenderAwardService {

    @Autowired private ETenderAwardDAO      awardDAO;
    @Autowired private ETenderBidDAO        bidDAO;
    @Autowired private ETenderNoticeDAO     noticeDAO;
    @Autowired private ProjectListService   projectListService;
    @Autowired private ProjectBudgetService projectBudgetService;
    @Autowired private EmailService         emailService;

    @Transactional
    public ETenderAward awardTender(ETenderAward award) {

        ETenderBid bid = bidDAO.getById(award.getBidId());
        if (bid == null)
            throw new RuntimeException("Bid পাওয়া যায়নি।");

        // FIX 7: bid টি সত্যিই এই tender-এর কিনা যাচাই করো
        if (bid.getTenderId() != award.getTenderId())
            throw new RuntimeException(
                "Bid #" + award.getBidId() + " এই Tender-এর নয়। Award দেওয়া সম্ভব নয়।");

        ETenderAward existing = awardDAO.getByTenderId(award.getTenderId());
        if (existing != null)
            throw new RuntimeException("এই Tender ইতোমধ্যে award করা হয়েছে।");

        ETenderNotice notice = noticeDAO.getById(award.getTenderId());
        if (notice == null)
            throw new RuntimeException("Tender notice পাওয়া যায়নি।");

        if (award.getAwardedAmount() == null || award.getAwardedAmount() == 0)
            award.setAwardedAmount(bid.getBidAmount());
        award.setAwardedTo(bid.getCompanyName());
        award.setAwardDate(LocalDate.now());
        award.setCreatedAt(LocalDateTime.now());

        bidDAO.updateStatus(award.getBidId(), "Selected");
        noticeDAO.updateStatus(award.getTenderId(), "Awarded");

        ETenderAward saved = awardDAO.save(award);

        // Auto-create Development Project
        ProjectList project = new ProjectList();
        project.setName(notice.getTitle());
        project.setLocation(notice.getWorkLocation() != null ? notice.getWorkLocation() : "TBD");
        project.setStartDate(LocalDate.now().toString());
        project.setEndDate(notice.getEndDate() != null
            ? notice.getEndDate().toString()
            : LocalDate.now().plusDays(bid.getCompletionDays()).toString());
        project.setStatus("Ongoing");
        project.setProgress(0);
        projectListService.create(project);

        // Auto-create Project Budget
        ProjectBudget budget = new ProjectBudget();
        budget.setName(notice.getTitle());
        budget.setBudget(award.getAwardedAmount());
        budget.setExpense(0);
        budget.setStatus("Active");
        projectBudgetService.create(budget);

        sendAwardWinnerEmail(bid, notice, award);
        sendRejectionEmailToOthers(award.getTenderId(), award.getBidId(), notice);

        return saved;
    }

    public List<ETenderAward> getAll()              { return awardDAO.getAll(); }
    public ETenderAward getByTenderId(int tenderId) { return awardDAO.getByTenderId(tenderId); }
    public ETenderAward getById(int id)             { return awardDAO.getById(id); }

    private void sendAwardWinnerEmail(ETenderBid bid, ETenderNotice notice, ETenderAward award) {
        if (bid.getEmail() == null || bid.getEmail().isBlank()) return;
        String subject = "🏆 টেন্ডার পুরস্কার বিজ্ঞপ্তি – " + notice.getTenderNo();
        emailService.sendHtml(bid.getEmail(), subject,
            buildWinnerHtml(bid, notice, award));
    }

    private void sendRejectionEmailToOthers(int tenderId, int winnerBidId, ETenderNotice notice) {
        List<ETenderBid> allBids = bidDAO.getByTenderId(tenderId);
        for (ETenderBid b : allBids) {
            if (b.getId() == winnerBidId) continue;
            if (b.getEmail() == null || b.getEmail().isBlank()) continue;
            bidDAO.updateStatus(b.getId(), "Not Selected");
            emailService.sendHtml(b.getEmail(),
                "টেন্ডার ফলাফল – " + notice.getTenderNo(),
                buildRejectionHtml(b.getBidderName(), notice.getTenderNo(), notice.getTitle()));
        }
    }

    private String buildWinnerHtml(ETenderBid bid, ETenderNotice notice, ETenderAward award) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
            + "<body style='font-family:Arial,sans-serif;background:#f1f5f9;padding:20px'>"
            + "<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:12px;"
            + "overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1)'>"
            + "<div style='background:linear-gradient(135deg,#1e3a5f,#1e40af);padding:28px;"
            + "border-bottom:4px solid #f59e0b'>"
            + "<h2 style='color:#fff;margin:0'>গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</h2>"
            + "<p style='color:#bfdbfe;margin:6px 0 0;font-size:13px'>E-Tender Management System</p></div>"
            + "<div style='background:#fefce8;padding:20px;text-align:center;border-bottom:1px solid #fde68a'>"
            + "<div style='font-size:48px'>🏆</div>"
            + "<h3 style='color:#92400e;margin:8px 0 0'>অভিনন্দন! আপনি নির্বাচিত হয়েছেন</h3></div>"
            + "<div style='padding:32px'>"
            + "<p>প্রিয় <strong>" + bid.getBidderName() + "</strong>,</p>"
            + "<div style='background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:16px;margin:20px 0'>"
            + "<table style='width:100%;border-collapse:collapse'>"
            + "<tr><td style='color:#64748b;font-size:13px;padding:7px 0'>টেন্ডার নম্বর</td>"
            + "<td style='font-weight:700'>" + notice.getTenderNo() + "</td></tr>"
            + "<tr><td style='color:#64748b;font-size:13px;padding:7px 0'>কাজের শিরোনাম</td>"
            + "<td>" + notice.getTitle() + "</td></tr>"
            + "<tr><td style='color:#64748b;font-size:13px;padding:7px 0'>পুরস্কারের পরিমাণ</td>"
            + "<td style='color:#059669;font-weight:700'>৳ " + String.format("%,.2f", award.getAwardedAmount()) + "</td></tr>"
            + "<tr><td style='color:#64748b;font-size:13px;padding:7px 0'>তারিখ</td>"
            + "<td>" + award.getAwardDate() + "</td></tr>"
            + (award.getRemarks() != null && !award.getRemarks().isBlank()
                ? "<tr><td style='color:#64748b;font-size:13px;padding:7px 0'>মন্তব্য</td>"
                + "<td>" + award.getRemarks() + "</td></tr>" : "")
            + "</table></div>"
            + "<p style='color:#065f46;background:#f0fdf4;border-left:4px solid #059669;"
            + "padding:12px;border-radius:4px'>⚠️ চুক্তি স্বাক্ষরের জন্য অফিসে যোগাযোগ করুন।</p>"
            + "</div><div style='background:#f8fafc;padding:16px;text-align:center;"
            + "border-top:1px solid #e2e8f0'><p style='color:#94a3b8;font-size:11px;margin:0'>"
            + "E-Tender Management System | গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</p></div>"
            + "</div></body></html>";
    }

    private String buildRejectionHtml(String bidderName, String tenderNo, String tenderTitle) {
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'></head>"
            + "<body style='font-family:Arial,sans-serif;background:#f1f5f9;padding:20px'>"
            + "<div style='max-width:600px;margin:0 auto;background:#fff;border-radius:12px;"
            + "overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,.1)'>"
            + "<div style='background:linear-gradient(135deg,#1e3a5f,#1e40af);padding:28px;"
            + "border-bottom:4px solid #f59e0b'>"
            + "<h2 style='color:#fff;margin:0'>গণপ্রজাতন্ত্রী বাংলাদেশ সরকার</h2>"
            + "<p style='color:#bfdbfe;margin:6px 0 0;font-size:13px'>E-Tender Management System</p></div>"
            + "<div style='padding:32px'>"
            + "<h3>টেন্ডার ফলাফল</h3>"
            + "<p>প্রিয় <strong>" + bidderName + "</strong>,</p>"
            + "<p>এই টেন্ডারে অংশগ্রহণের জন্য ধন্যবাদ। আপনার বিড নির্বাচিত হয়নি।</p>"
            + "<div style='background:#f8fafc;border:1px solid #e2e8f0;border-radius:8px;padding:16px'>"
            + "<table style='width:100%;border-collapse:collapse'>"
            + "<tr><td style='color:#64748b;padding:7px 0'>টেন্ডার নম্বর</td>"
            + "<td style='font-weight:700'>" + tenderNo + "</td></tr>"
            + "<tr><td style='color:#64748b;padding:7px 0'>শিরোনাম</td>"
            + "<td>" + tenderTitle + "</td></tr></table></div>"
            + "<p style='color:#6b7280;font-size:13px'>ভবিষ্যতে অন্য Tender-এ অংশগ্রহণের আমন্ত্রণ।</p>"
            + "</div></div></body></html>";
    }
}
