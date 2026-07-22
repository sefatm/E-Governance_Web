package com.mgt.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.mgt.dao.ETenderBidDAO;
import com.mgt.dao.ETenderNoticeDAO;
import com.mgt.model.ETenderNotice;

/**
 * IMPORTANT: GovernanceApplication.java তে @EnableScheduling যোগ করুন:
 *
 *   @SpringBootApplication
 *   @EnableScheduling          ← এটা যোগ করুন
 *   public class GovernanceApplication { ... }
 *
 * না হলে autoClosePastDeadlineTenders() কখনো চলবে না।
 */
@Service
public class ETenderNoticeService {

    @Autowired private ETenderNoticeDAO noticeDAO;
    // FIX 10: delete এর আগে bid count check করতে BidDAO দরকার
    @Autowired private ETenderBidDAO    bidDAO;

    public ETenderNotice create(ETenderNotice notice) {
        String year     = String.valueOf(LocalDate.now().getYear());
        String tenderNo = "TND-" + year + "-" + System.currentTimeMillis() % 10000;
        notice.setTenderNo(tenderNo);
        notice.setStatus("Open");
        return noticeDAO.save(notice);
    }

    public List<ETenderNotice> getAll()  { return noticeDAO.getAll(); }
    public List<ETenderNotice> getOpen() { return noticeDAO.getByStatus("Open"); }
    public ETenderNotice getById(int id) { return noticeDAO.getById(id); }

    public void updateStatus(int id, String status) {
        noticeDAO.updateStatus(id, status);
    }

    public ETenderNotice update(int id, ETenderNotice updated) {
        ETenderNotice existing = noticeDAO.getById(id);
        if (existing == null) throw new RuntimeException("Tender পাওয়া যায়নি: " + id);
        existing.setTitle(updated.getTitle());
        existing.setCategory(updated.getCategory());
        existing.setDescription(updated.getDescription());
        existing.setEstimatedCost(updated.getEstimatedCost());
        existing.setEmdAmount(updated.getEmdAmount());
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setWorkLocation(updated.getWorkLocation());
        return noticeDAO.update(existing);
    }

    /**
     * FIX 10: delete এর আগে bid count check করো।
     * bid থাকলে delete করা যাবে না — FK constraint error এড়ানো হবে।
     */
    public void delete(int id) {
        long bidCount = bidDAO.countByTenderId(id);
        if (bidCount > 0) {
            throw new RuntimeException(
                "এই Tender এ " + bidCount + "টি Bid আছে। Bid আছে এমন Tender delete করা যাবে না।"
            );
        }
        noticeDAO.delete(id);
    }

    // FIX 5: @Scheduled কাজ করতে @EnableScheduling লাগবে — GovernanceApplication.java দেখুন
    @Scheduled(cron = "0 1 0 * * *")
    public void autoClosePastDeadlineTenders() {
        List<ETenderNotice> openTenders = noticeDAO.getByStatus("Open");
        LocalDate today = LocalDate.now();
        int closedCount = 0;
        for (ETenderNotice notice : openTenders) {
            if (notice.getEndDate() != null && notice.getEndDate().isBefore(today)) {
                noticeDAO.updateStatus(notice.getId(), "Closed");
                closedCount++;
            }
        }
        if (closedCount > 0)
            System.out.println("[ETender Scheduler] " + closedCount + " টি Tender auto-close হয়েছে। তারিখ: " + today);
    }

    public int manualCloseExpired() {
        List<ETenderNotice> openTenders = noticeDAO.getByStatus("Open");
        LocalDate today = LocalDate.now();
        int count = 0;
        for (ETenderNotice notice : openTenders) {
            if (notice.getEndDate() != null && notice.getEndDate().isBefore(today)) {
                noticeDAO.updateStatus(notice.getId(), "Closed");
                count++;
            }
        }
        return count;
    }
}
