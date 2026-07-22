package com.mgt.service;

import com.mgt.dao.LpgCardDAO;
import com.mgt.dao.LpgDistributionDAO;
import com.mgt.dao.LpgStockDAO;
import com.mgt.model.LpgCard;
import com.mgt.model.LpgDistributionLog;
import com.mgt.model.LpgStock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class LpgCardService {

    @Autowired LpgCardDAO         lpgCardDAO;
    @Autowired LpgDistributionDAO distributionDAO;
    @Autowired LpgStockDAO        stockDAO;
    @Autowired ApplicationEmailNotifier emailNotifier;

    // ══════════════════════════════════════════════════════════
    // SECTION 1 — CARD MANAGEMENT (আগের মতো, অপরিবর্তিত)
    // ══════════════════════════════════════════════════════════

    public String create(LpgCard card) {
        if (card.isHasGasLine()) return "HAS_GAS_LINE";
        LpgCard existing = lpgCardDAO.getByNid(card.getNid());
        if (existing != null) return "DUPLICATE";
        lpgCardDAO.save(card);
        emailNotifier.sendApplicationReceived(
                card.getContact(), card.getHolderName(), "এলপিজি কার্ড", card.getCardNo());
        return "OK";
    }

    public void updateStatus(int id, String status, String approvedBy, String rejectionReason) {
        updateStatus(id, status, approvedBy, rejectionReason, null);
    }

    public List<LpgCard> getAll()                           { return lpgCardDAO.getAll(); }
    public List<LpgCard> getByStatus(String status)         { return lpgCardDAO.getByStatus(status); }
    public List<LpgCard> getByDealer(String dealerCode)     { return lpgCardDAO.getByDealer(dealerCode); }
    public List<LpgCard> getByDistrict(String district)     { return lpgCardDAO.getByDistrict(district); }
    public LpgCard getById(int id)                          { return lpgCardDAO.getById(id); }
    public LpgCard getByNid(String nid)                     { return lpgCardDAO.getByNid(nid); }

    public void updateStatus(int id, String status, String approvedBy, String rejectionReason, String signatureBase64) {
        lpgCardDAO.updateStatus(id, status, approvedBy, rejectionReason);
        if (signatureBase64 != null && !signatureBase64.isBlank()) {
            LpgCard card = lpgCardDAO.getById(id);
            if (card != null) {
                card.setCertificateSignature(signatureBase64);
                lpgCardDAO.updateOnly(card);
            }
        }
        LpgCard card = lpgCardDAO.getById(id);
        if (card != null) {
            if ("Approved".equalsIgnoreCase(status)) {
                // ✅ Approval — dealer info সহ ৭ কর্মদিবসের মধ্যে কার্ড সংগ্রহের নির্দেশ
                emailNotifier.sendCardApprovedWithPickupInfo(
                        card.getContact(),
                        card.getHolderName(),
                        "এলপিজি কার্ড",
                        card.getCardNo(),
                        card.getDealerName(),
                        card.getDealerContact()
                );
            } else {
                emailNotifier.sendStatusUpdate(
                        card.getContact(), card.getHolderName(),
                        "এলপিজি কার্ড", card.getCardNo(), status, rejectionReason);
            }
        }
    }

    public void delete(int id) { lpgCardDAO.delete(id); }

    // ══════════════════════════════════════════════════════════
    // SECTION 2 — CYLINDER DISTRIBUTION (নতুন)
    // ══════════════════════════════════════════════════════════

    /**
     * Dealer কার্ডধারীকে সিলিন্ডার দেওয়ার রেকর্ড।
     *
     * Checks:
     *  1. Card must be Approved
     *  2. Not already collected this cycle (monthlyQuota মেনে)
     *  3. Stock sufficient হলে deduct করো, না হলে warn করো (block করো না)
     */
    public Map<String, Object> recordDistribution(int cardId, String cycleMonth,
                                                   int cylindersQty, String collectedBy) {
        LpgCard card = lpgCardDAO.getById(cardId);
        if (card == null)
            return fail("কার্ড পাওয়া যায়নি।");
        if (!"Approved".equalsIgnoreCase(card.getStatus()))
            return fail("কার্ডটি অনুমোদিত নয়। স্ট্যাটাস: " + card.getStatus());

        // Duplicate check for this cycle
        if (distributionDAO.alreadyCollectedThisCycle(cardId, cycleMonth))
            return fail("'" + cycleMonth + "' চক্রে এই কার্ডে ইতিমধ্যে সিলিন্ডার বিতরণ হয়েছে।");

        // Quota check
        int quota = card.getMonthlyQuota() > 0 ? card.getMonthlyQuota() : 1;
        if (cylindersQty > quota)
            return fail("কোটা অতিক্রম করেছে। সর্বোচ্চ: " + quota + " সিলিন্ডার।");

        // Stock check — warn but don't block if no stock entry
        LpgStock stock = stockDAO.getLatestByCycle(cycleMonth);
        boolean stockWarning = false;
        if (stock != null) {
            if (stock.getRemaining() < cylindersQty)
                return fail("সিলিন্ডার মজুদ অপর্যাপ্ত। অবশিষ্ট: " + stock.getRemaining() + "টি।");
        } else {
            stockWarning = true; // no stock entry, proceed with warning
        }

        // Persist distribution log
        LpgDistributionLog log = new LpgDistributionLog();
        log.setCardId(cardId);
        log.setCardNo(card.getCardNo());
        log.setHolderName(card.getHolderName());
        log.setNid(card.getNid());
        log.setContact(card.getContact());
        log.setWard(card.getWard());
        log.setDistrict(card.getDistrict());
        log.setCycleMonth(cycleMonth);
        log.setCylindersQty(cylindersQty);
        log.setCylinderSize(card.getCylinderSize());
        log.setDealerName(card.getDealerName());
        log.setDealerCode(card.getDealerCode());
        log.setCollectedBy(collectedBy != null ? collectedBy : "System");
        if (stock != null) log.setStockId(stock.getId());
        distributionDAO.save(log);

        // Update LpgCard.lastCollectedAt
        lpgCardDAO.recordCollection(cardId);

        // Deduct stock
        if (stock != null) {
            stockDAO.deductStock(stock.getId(), cylindersQty);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success",      true);
        res.put("logId",        log.getId());
        res.put("holderName",   card.getHolderName());
        res.put("cardNo",       card.getCardNo());
        res.put("cylindersQty", cylindersQty);
        res.put("cycleMonth",   cycleMonth);
        res.put("stockUpdated", stock != null);
        if (stockWarning)
            res.put("warning", "স্টক এন্ট্রি নেই — মজুদ চেক করা হয়নি।");
        res.put("message", card.getHolderName() + " কে " + cylindersQty + "টি সিলিন্ডার বিতরণ সম্পন্ন ✓");

        // ✅ Distribution confirmation email to citizen
        String items = cylindersQty + "টি সিলিন্ডার"
                + (card.getCylinderSize() != null ? " (" + card.getCylinderSize() + ")" : "");
        emailNotifier.sendDistributionConfirmation(
                card.getContact(),
                card.getHolderName(),
                card.getCardNo(),
                "এলপিজি কার্ড",
                cycleMonth,
                items,
                collectedBy
        );
        return res;
    }
    
    public LpgCard getByCardNo(String cardNo) {
        return lpgCardDAO.getByCardNo(cardNo);
    }

    // ══════════════════════════════════════════════════════════
    // SECTION 3 — HISTORY (নতুন)
    // ══════════════════════════════════════════════════════════

    public List<LpgDistributionLog> getHistoryByCardId(int cardId) {
        return distributionDAO.getByCardId(cardId);
    }

    public List<LpgDistributionLog> getHistoryByCardNo(String cardNo) {
        return distributionDAO.getByCardNo(cardNo);
    }

    public Map<String, Object> getCycleSummary(String cycleMonth) {
        List<LpgDistributionLog> logs = distributionDAO.getByCycle(cycleMonth);
        int totalCylinders = logs.stream().mapToInt(LpgDistributionLog::getCylindersQty).sum();
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("cycleMonth",      cycleMonth);
        res.put("totalCards",      logs.size());
        res.put("totalCylinders",  totalCylinders);
        res.put("logs",            logs);
        return res;
    }

    public List<LpgDistributionLog> getDealerHistory(String cycleMonth, String dealerCode) {
        return distributionDAO.getByCycleAndDealer(cycleMonth, dealerCode);
    }

    // ══════════════════════════════════════════════════════════
    // SECTION 4 — STOCK MANAGEMENT (নতুন)
    // ══════════════════════════════════════════════════════════

    public List<LpgStock> getStockList(String cycleMonth) {
        if (cycleMonth != null && !cycleMonth.isBlank())
            return stockDAO.getByCycle(cycleMonth);
        return stockDAO.getAll();
    }

    public Map<String, Object> saveStock(String cycleMonth, String batchLabel,
                                          String ward, String dealerName, String dealerCode,
                                          String cylinderSize, int totalCylinders, int totalCards) {
        if (totalCylinders <= 0)
            return fail("সিলিন্ডার সংখ্যা ০-এর বেশি হতে হবে।");

        LpgStock s = new LpgStock();
        s.setCycleMonth(cycleMonth);
        s.setBatchLabel(batchLabel);
        s.setWard(ward);
        s.setDealerName(dealerName);
        s.setDealerCode(dealerCode);
        s.setCylinderSize(cylinderSize != null ? cylinderSize : "12kg");
        s.setTotalCylinders(totalCylinders);
        s.setTotalCards(totalCards);
        s.setDistributed(0);
        stockDAO.save(s);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success",        true);
        res.put("id",             s.getId());
        res.put("cycleMonth",     cycleMonth);
        res.put("totalCylinders", totalCylinders);
        res.put("message",        "স্টক সফলভাবে সংরক্ষিত হয়েছে।");
        return res;
    }

    // ── Helper ────────────────────────────────────────────────
    private Map<String, Object> fail(String msg) {
        return Map.of("success", false, "message", msg);
    }
}
