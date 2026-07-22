package com.mgt.service;

import com.mgt.dao.FarmerCardDAO;
import com.mgt.dao.FarmerStockDAO;
import com.mgt.model.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
public class FarmerDistributionService {

    @PersistenceContext
    private EntityManager em;

    @Autowired FarmerCardDAO  farmerCardDAO;
    @Autowired FarmerStockDAO farmerStockDAO;
    @Autowired ApplicationEmailNotifier emailNotifier;   // ← নতুন

    // ══════════════════════════════════════════════════════════
    // SECTION 1 — PHYSICAL SUBSIDY (সার / বীজ)
    // ══════════════════════════════════════════════════════════

    /**
     * Record physical fertilizer/seed distribution for a farmer.
     * Called after QR scan confirms card is valid.
     *
     * Checks:
     *  1. Card must be Approved
     *  2. Land must be verified
     *  3. Not already distributed this cycle
     *  4. Quantity must not exceed farmer quota
     *  5. Stock must be sufficient (নতুন — সার/বীজ মজুদ চেক)
     */
    public Map<String, Object> distribute(int cardId, String cycleMonth, String season,
                                          BigDecimal fertilizerKg, BigDecimal seedKg,
                                          BigDecimal pesticideLitre, String distributedBy,
                                          Integer sessionId) {

        FarmerCard card = farmerCardDAO.getById(cardId);
        if (card == null)
            return fail("কার্ড পাওয়া যায়নি।");
        if (!"Approved".equalsIgnoreCase(card.getStatus()))
            return fail("কার্ডটি অনুমোদিত নয়। স্ট্যাটাস: " + card.getStatus());
        if (!Boolean.TRUE.equals(card.getLandVerified()))
            return fail("জমি যাচাই হয়নি। বিতরণের আগে জমি যাচাই করতে হবে।");

        // Duplicate check
        if (alreadyDistributedThisCycle(cardId, cycleMonth))
            return fail("এই কার্ডে '" + cycleMonth + "' চক্রে ইতিমধ্যে বিতরণ করা হয়েছে।");

        // Farmer quota guard
        BigDecimal quota = card.getFertilizerQuota() != null ? card.getFertilizerQuota() : BigDecimal.ZERO;
        if (fertilizerKg != null && fertilizerKg.compareTo(quota) > 0)
            return fail("সার কোটা অতিক্রম করেছে। সর্বোচ্চ: " + quota + " কেজি।");

        BigDecimal seedQuota = card.getSeedQuota() != null ? card.getSeedQuota() : BigDecimal.ZERO;
        if (seedKg != null && seedKg.compareTo(seedQuota) > 0)
            return fail("বীজ কোটা অতিক্রম করেছে। সর্বোচ্চ: " + seedQuota + " কেজি।");

        // ── Stock availability check (নতুন) ──────────────────
        FarmerStock stock = farmerStockDAO.getLatestByCycle(cycleMonth);
        if (stock != null) {
            BigDecimal fertRemaining = stock.getFertilizerRemaining();
            BigDecimal seedRemaining = stock.getSeedRemaining();
            BigDecimal fertNeeded    = fertilizerKg  != null ? fertilizerKg  : BigDecimal.ZERO;
            BigDecimal seedNeeded    = seedKg         != null ? seedKg        : BigDecimal.ZERO;

            if (fertNeeded.compareTo(BigDecimal.ZERO) > 0 &&
                fertNeeded.compareTo(fertRemaining) > 0)
                return fail("সার মজুদ অপর্যাপ্ত। অবশিষ্ট: " + fertRemaining + " kg, প্রয়োজন: " + fertNeeded + " kg।");

            if (seedNeeded.compareTo(BigDecimal.ZERO) > 0 &&
                seedNeeded.compareTo(seedRemaining) > 0)
                return fail("বীজ মজুদ অপর্যাপ্ত। অবশিষ্ট: " + seedRemaining + " kg, প্রয়োজন: " + seedNeeded + " kg।");
        }
        // stock == null হলে: stock entry না থাকলে block করব না, শুধু warn করব
        // (admins নতুন setup-এ প্রথমে stock ছাড়াও test করতে পারেন)

        // Determine subsidy type
        String type = "MIXED";
        if ((seedKg == null || seedKg.compareTo(BigDecimal.ZERO) == 0) &&
            (pesticideLitre == null || pesticideLitre.compareTo(BigDecimal.ZERO) == 0))
            type = "FERTILIZER";
        else if ((fertilizerKg == null || fertilizerKg.compareTo(BigDecimal.ZERO) == 0) &&
                 (pesticideLitre == null || pesticideLitre.compareTo(BigDecimal.ZERO) == 0))
            type = "SEED";

        // Persist distribution log
        FarmerSubsidyLog log = new FarmerSubsidyLog();
        log.setCardId(cardId);
        log.setCardNo(card.getCardNo());
        log.setFarmerName(card.getFarmerName());
        log.setNid(card.getNid());
        log.setWard(card.getWard());
        log.setDistrict(card.getDistrict());
        log.setSubsidyType(type);
        log.setFertilizerKg(fertilizerKg    != null ? fertilizerKg    : BigDecimal.ZERO);
        log.setSeedKg(seedKg                != null ? seedKg          : BigDecimal.ZERO);
        log.setPesticideLitre(pesticideLitre!= null ? pesticideLitre  : BigDecimal.ZERO);
        log.setSeason(season);
        log.setCycleMonth(cycleMonth);
        log.setDistDate(LocalDate.now());
        log.setDistributedBy(distributedBy);
        log.setSessionId(sessionId);
        log.setCreatedAt(LocalDateTime.now());
        em.persist(log);
        em.flush();

        // ── Deduct from stock (নতুন) ──────────────────────────
        if (stock != null) {
            farmerStockDAO.deductStock(
                    stock.getId(),
                    fertilizerKg != null ? fertilizerKg : BigDecimal.ZERO,
                    seedKg       != null ? seedKg       : BigDecimal.ZERO
            );
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success",        true);
        res.put("farmerName",     card.getFarmerName());
        res.put("cardNo",         card.getCardNo());
        res.put("fertilizerKg",   log.getFertilizerKg());
        res.put("seedKg",         log.getSeedKg());
        res.put("pesticideLitre", log.getPesticideLitre());
        res.put("distDate",       LocalDate.now().toString());
        res.put("logId",          log.getId());
        res.put("stockUpdated",   stock != null);
        res.put("message",        card.getFarmerName() + " কে সার/বীজ বিতরণ সম্পন্ন ✓");

        // ✅ Distribution confirmation email to farmer
        StringBuilder items = new StringBuilder();
        if (fertilizerKg != null && fertilizerKg.compareTo(BigDecimal.ZERO) > 0)
            items.append("সার: ").append(fertilizerKg).append(" কেজি");
        if (seedKg != null && seedKg.compareTo(BigDecimal.ZERO) > 0) {
            if (items.length() > 0) items.append(", ");
            items.append("বীজ: ").append(seedKg).append(" কেজি");
        }
        if (pesticideLitre != null && pesticideLitre.compareTo(BigDecimal.ZERO) > 0) {
            if (items.length() > 0) items.append(", ");
            items.append("কীটনাশক: ").append(pesticideLitre).append(" লিটার");
        }
        emailNotifier.sendDistributionConfirmation(
                card.getContact(),
                card.getFarmerName(),
                card.getCardNo(),
                "কৃষক কার্ড",
                cycleMonth,
                items.length() > 0 ? items.toString() : "কৃষি উপকরণ বিতরণ",
                distributedBy
        );
        return res;
    }

    // ══════════════════════════════════════════════════════════
    // SECTION 2 — STOCK MANAGEMENT (নতুন)
    // ══════════════════════════════════════════════════════════

    /**
     * GET /api/farmer/stock?cycleMonth=YYYY-MM
     * History page-এ স্টক টেবিল লোড করে।
     */
    public List<FarmerStock> getStockList(String cycleMonth) {
        if (cycleMonth != null && !cycleMonth.isBlank())
            return farmerStockDAO.getByCycle(cycleMonth);
        return farmerStockDAO.getAll();
    }

    /**
     * POST /api/farmer/stock
     * Admin নতুন স্টক এন্ট্রি সেভ করেন।
     */
    public Map<String, Object> saveStock(String cycleMonth, String batchNo,
                                          BigDecimal fertilizerKg, BigDecimal seedKg,
                                          BigDecimal pesticideLitre, String note) {
        FarmerStock s = new FarmerStock();
        s.setCycleMonth(cycleMonth);
        s.setBatchNo(batchNo);
        s.setFertilizerKg(fertilizerKg  != null ? fertilizerKg  : BigDecimal.ZERO);
        s.setSeedKg(seedKg              != null ? seedKg        : BigDecimal.ZERO);
        s.setPesticideLitre(pesticideLitre != null ? pesticideLitre : BigDecimal.ZERO);
        s.setNote(note);
        farmerStockDAO.save(s);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success",      true);
        res.put("id",           s.getId());
        res.put("cycleMonth",   cycleMonth);
        res.put("fertilizerKg", s.getFertilizerKg());
        res.put("seedKg",       s.getSeedKg());
        res.put("message",      "স্টক সফলভাবে সংরক্ষিত হয়েছে।");
        return res;
    }

    // ══════════════════════════════════════════════════════════
    // SECTION 3 — HISTORY QUERIES
    // ══════════════════════════════════════════════════════════

    public List<FarmerSubsidyLog> getSubsidyHistory(int cardId) {
        return em.createQuery(
                "FROM FarmerSubsidyLog WHERE cardId = :id ORDER BY createdAt DESC",
                FarmerSubsidyLog.class)
                .setParameter("id", cardId)
                .getResultList();
    }

    /** Angular-এর cycle-summary/by-card/{cardNo} endpoint-এর জন্য */
    public List<FarmerSubsidyLog> getSubsidyHistoryByCardNo(String cardNo) {
        return em.createQuery(
                "FROM FarmerSubsidyLog WHERE cardNo = :c ORDER BY createdAt DESC",
                FarmerSubsidyLog.class)
                .setParameter("c", cardNo)
                .getResultList();
    }

    public List<FarmerSubsidyLog> getSubsidyByCycle(String cycleMonth) {
        return em.createQuery(
                "FROM FarmerSubsidyLog WHERE cycleMonth = :c ORDER BY createdAt DESC",
                FarmerSubsidyLog.class)
                .setParameter("c", cycleMonth)
                .getResultList();
    }

    public Map<String, Object> getCycleSummary(String cycleMonth) {
        List<FarmerSubsidyLog> logs = getSubsidyByCycle(cycleMonth);
        BigDecimal totalFert = logs.stream().map(FarmerSubsidyLog::getFertilizerKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalSeed = logs.stream().map(FarmerSubsidyLog::getSeedKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of(
                "cycleMonth",        cycleMonth,
                "totalFarmers",      logs.size(),
                "totalFertilizerKg", totalFert,
                "totalSeedKg",       totalSeed,
                "logs",              logs
        );
    }

    private boolean alreadyDistributedThisCycle(int cardId, String cycleMonth) {
        Long count = em.createQuery(
                "SELECT COUNT(l) FROM FarmerSubsidyLog l WHERE l.cardId = :id AND l.cycleMonth = :c",
                Long.class)
                .setParameter("id", cardId)
                .setParameter("c", cycleMonth)
                .getSingleResult();
        return count > 0;
    }

    // ══════════════════════════════════════════════════════════
    // SECTION 4 — G2P BANK TRANSFER (অপরিবর্তিত)
    // ══════════════════════════════════════════════════════════

    public Map<String, Object> createG2pBatch(String cycleMonth, String ward,
                                               String district, BigDecimal amountPerFarmer,
                                               String gateway, String submittedBy) {
        List<G2pBeneficiaryBatch> existing = em.createQuery(
                "FROM G2pBeneficiaryBatch WHERE cycleMonth=:c AND (ward=:w OR :w IS NULL) AND status NOT IN ('FAILED')",
                G2pBeneficiaryBatch.class)
                .setParameter("c", cycleMonth)
                .setParameter("w", ward)
                .getResultList();
        if (!existing.isEmpty())
            return fail("এই cycle-এ ইতিমধ্যে একটি batch আছে। Batch Ref: " + existing.get(0).getBatchRef());

        String jpql = "FROM farmerCard WHERE status='Approved'" +
                (ward     != null && !ward.isEmpty()     ? " AND ward=:ward"         : "") +
                (district != null && !district.isEmpty() ? " AND district=:district" : "");
        var q = em.createQuery(jpql, FarmerCard.class);
        if (ward     != null && !ward.isEmpty())     q.setParameter("ward",     ward);
        if (district != null && !district.isEmpty()) q.setParameter("district", district);
        List<FarmerCard> farmers = q.getResultList();

        if (farmers.isEmpty())
            return fail("নির্বাচিত ward/district-এ কোনো Approved কৃষক কার্ড নেই।");

        List<String> noAccount = new ArrayList<>();
        for (FarmerCard fc : farmers)
            if (fc.getBankAccount() == null || fc.getBankAccount().isBlank())
                noAccount.add(fc.getFarmerName() + " (" + fc.getCardNo() + ")");

        G2pBeneficiaryBatch batch = new G2pBeneficiaryBatch();
        batch.setBatchRef("G2P-" + cycleMonth + "-" +
                String.format("%04d", (int)(Math.random() * 9000) + 1000));
        batch.setCycleMonth(cycleMonth);
        batch.setWard(ward);
        batch.setDistrict(district);
        batch.setTotalFarmers(farmers.size());
        batch.setAmountPerFarmer(amountPerFarmer);
        batch.setTotalAmount(amountPerFarmer.multiply(BigDecimal.valueOf(farmers.size())));
        batch.setGateway(gateway != null ? gateway : "BEFTN");
        batch.setStatus("DRAFT");
        batch.setSubmittedBy(submittedBy);
        batch.setCreatedAt(LocalDateTime.now());
        em.persist(batch);
        em.flush();

        int created = 0;
        for (FarmerCard fc : farmers) {
            if (fc.getBankAccount() == null || fc.getBankAccount().isBlank()) continue;
            G2pTransfer t = new G2pTransfer();
            t.setBatchId(batch.getId());
            t.setCardId(fc.getId());
            t.setCardNo(fc.getCardNo());
            t.setFarmerName(fc.getFarmerName());
            t.setNid(fc.getNid());
            t.setMobile(fc.getContact());
            t.setBankName(fc.getBankName());
            t.setBankAccount(fc.getBankAccount());
            t.setBankBranch(fc.getBankBranch());
            t.setAmount(amountPerFarmer);
            t.setGateway(batch.getGateway());
            t.setTxnRef("TXN-" + batch.getBatchRef() + "-" + fc.getCardNo());
            t.setStatus("PENDING");
            em.persist(t);
            created++;
        }
        em.flush();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success",          true);
        res.put("batchId",          batch.getId());
        res.put("batchRef",         batch.getBatchRef());
        res.put("totalFarmers",     farmers.size());
        res.put("transfersCreated", created);
        res.put("totalAmount",      batch.getTotalAmount());
        res.put("gateway",          batch.getGateway());
        res.put("noAccountCount",   noAccount.size());
        if (!noAccount.isEmpty()) res.put("noAccountList", noAccount);
        res.put("message", "Batch তৈরি হয়েছে। Submit করলে bank transfer শুরু হবে।");
        return res;
    }

    public Map<String, Object> submitBatch(int batchId, String submittedBy) {
        G2pBeneficiaryBatch batch = em.find(G2pBeneficiaryBatch.class, batchId);
        if (batch == null) return fail("Batch পাওয়া যায়নি।");
        if (!"DRAFT".equals(batch.getStatus()))
            return fail("শুধুমাত্র DRAFT batch submit করা যাবে।");

        batch.setStatus("SUBMITTED");
        batch.setSubmittedBy(submittedBy);
        batch.setSubmittedAt(LocalDateTime.now());
        em.merge(batch);

        List<G2pTransfer> transfers = getTransfersByBatch(batchId);
        for (G2pTransfer t : transfers) {
            if ("PENDING".equals(t.getStatus())) {
                t.setStatus("PROCESSING");
                t.setInitiatedAt(LocalDateTime.now());
                em.merge(t);
            }
        }
        em.flush();

        return Map.of("success", true, "batchRef", batch.getBatchRef(),
                "submitted", transfers.size(),
                "message", "Batch submit হয়েছে। Bank transfer শুরু হয়েছে।");
    }

    public Map<String, Object> handleCallback(String txnRef, String providerTxnId,
                                               String status, String failureReason) {
        List<G2pTransfer> rows = em.createQuery(
                "FROM G2pTransfer WHERE txnRef=:ref", G2pTransfer.class)
                .setParameter("ref", txnRef)
                .getResultList();

        if (rows.isEmpty())
            return fail("txnRef '" + txnRef + "' পাওয়া যায়নি।");

        G2pTransfer t = rows.get(0);
        t.setProviderTxnId(providerTxnId);
        t.setStatus("SUCCESS".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)
                ? "COMPLETED" : "FAILED");
        if (failureReason != null) t.setFailureReason(failureReason);
        if ("COMPLETED".equals(t.getStatus())) t.setCompletedAt(LocalDateTime.now());
        em.merge(t);
        updateBatchStatusIfDone(t.getBatchId());
        em.flush();

        return Map.of("success", true, "txnRef", txnRef, "newStatus", t.getStatus());
    }

    public Map<String, Object> retryFailed(int batchId) {
        List<G2pTransfer> failed = em.createQuery(
                "FROM G2pTransfer WHERE batchId=:id AND status='FAILED' AND retryCount < 3",
                G2pTransfer.class)
                .setParameter("id", batchId)
                .getResultList();
        int retried = 0;
        for (G2pTransfer t : failed) {
            t.setStatus("PROCESSING");
            t.setInitiatedAt(LocalDateTime.now());
            t.setRetryCount(t.getRetryCount() + 1);
            t.setFailureReason(null);
            em.merge(t);
            retried++;
        }
        em.flush();
        return Map.of("success", true, "retried", retried,
                "message", retried + "টি failed transfer retry করা হয়েছে।");
    }

    public List<G2pBeneficiaryBatch> getAllBatches() {
        return em.createQuery("FROM G2pBeneficiaryBatch ORDER BY createdAt DESC",
                G2pBeneficiaryBatch.class).getResultList();
    }

    public List<G2pTransfer> getTransfersByBatch(int batchId) {
        return em.createQuery(
                "FROM G2pTransfer WHERE batchId=:id ORDER BY createdAt",
                G2pTransfer.class)
                .setParameter("id", batchId)
                .getResultList();
    }

    public Map<String, Object> getBatchSummary(int batchId) {
        List<G2pTransfer> all = getTransfersByBatch(batchId);
        long completed  = all.stream().filter(t -> "COMPLETED".equals(t.getStatus())).count();
        long failed     = all.stream().filter(t -> "FAILED".equals(t.getStatus())).count();
        long pending    = all.stream().filter(t -> "PENDING".equals(t.getStatus())).count();
        BigDecimal paidAmt = all.stream().filter(t -> "COMPLETED".equals(t.getStatus()))
                .map(G2pTransfer::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        G2pBeneficiaryBatch batch = em.find(G2pBeneficiaryBatch.class, batchId);
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("batch",      batch);
        res.put("total",      all.size());
        res.put("completed",  completed);
        res.put("failed",     failed);
        res.put("pending",    pending);
        res.put("totalPaid",  paidAmt);
        res.put("transfers",  all);
        return res;
    }

    // ── INTERNAL HELPERS ──────────────────────────────────────

    private void updateBatchStatusIfDone(int batchId) {
        List<G2pTransfer> all = getTransfersByBatch(batchId);
        boolean anyPending   = all.stream().anyMatch(t -> "PENDING".equals(t.getStatus()) || "PROCESSING".equals(t.getStatus()));
        boolean anyCompleted = all.stream().anyMatch(t -> "COMPLETED".equals(t.getStatus()));
        boolean anyFailed    = all.stream().anyMatch(t -> "FAILED".equals(t.getStatus()));

        G2pBeneficiaryBatch batch = em.find(G2pBeneficiaryBatch.class, batchId);
        if (batch == null) return;
        if (!anyPending) {
            if (anyFailed && anyCompleted)  batch.setStatus("PARTIAL");
            else if (anyFailed)             batch.setStatus("FAILED");
            else                            batch.setStatus("COMPLETED");
            if ("COMPLETED".equals(batch.getStatus()) || "PARTIAL".equals(batch.getStatus()))
                batch.setCompletedAt(LocalDateTime.now());
            em.merge(batch);
        }
    }

    private Map<String, Object> fail(String msg) {
        return Map.of("success", false, "message", msg);
    }
}
