package com.mgt.service;

import com.mgt.model.VgdCard;
import com.mgt.model.VgdSession;
import com.mgt.model.VgdStock;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class VgdDistributionService {

    @PersistenceContext
    private EntityManager em;

    // ══════════════════════════════════════════════════════════
    // STOCK MANAGEMENT
    // ══════════════════════════════════════════════════════════

    public VgdStock createStock(VgdStock s) {
        if (s.getDistributed() < 0) s.setDistributed(0);
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        em.persist(s);
        em.flush();
        return s;
    }

    public List<VgdStock> getAllStock() {
        return em.createQuery(
                "FROM VgdStock ORDER BY cycleMonth DESC, cardType, ward",
                VgdStock.class).getResultList();
    }

    public VgdStock getStockById(int id) {
        return em.find(VgdStock.class, id);
    }

    public VgdStock getStockByCycleWardType(String cycle, String ward, String cardType) {
        List<VgdStock> res = em.createQuery(
                "FROM VgdStock WHERE cycleMonth=:c AND ward=:w AND cardType=:t",
                VgdStock.class)
                .setParameter("c", cycle)
                .setParameter("w", ward)
                .setParameter("t", cardType.toUpperCase())
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }

    private void deductStock(int stockId, BigDecimal rice, BigDecimal wheat, BigDecimal cash) {
        em.createQuery("""
            UPDATE VgdStock s SET
              s.riceKg     = s.riceKg     - :rice,
              s.wheatKg    = s.wheatKg    - :wheat,
              s.cashAmount = s.cashAmount - :cash,
              s.distributed = s.distributed + 1,
              s.updatedAt  = :now
            WHERE s.id = :id
            """)
          .setParameter("rice",  rice  != null ? rice  : BigDecimal.ZERO)
          .setParameter("wheat", wheat != null ? wheat : BigDecimal.ZERO)
          .setParameter("cash",  cash  != null ? cash  : BigDecimal.ZERO)
          .setParameter("now",   LocalDateTime.now())
          .setParameter("id",    stockId)
          .executeUpdate();
    }

    // ══════════════════════════════════════════════════════════
    // SESSION MANAGEMENT
    // ══════════════════════════════════════════════════════════

    public Map<String, Object> openSession(String dealerName, String ward,
                                            String cycleMonth, String cardType) {
        // Reuse existing open session for same ward+cycle+type
        List<VgdSession> existing = em.createQuery(
                "FROM VgdSession WHERE ward=:w AND cycleMonth=:c AND cardType=:t AND status='OPEN'",
                VgdSession.class)
                .setParameter("w", ward)
                .setParameter("c", cycleMonth)
                .setParameter("t", cardType.toUpperCase())
                .getResultList();

        if (!existing.isEmpty()) {
            VgdSession s = existing.get(0);
            return Map.of("sessionId", s.getId(), "sessionCode", s.getSessionCode(),
                    "message", "বিদ্যমান session পুনরায় ব্যবহার করা হচ্ছে।", "reused", true);
        }

        VgdStock stock = getStockByCycleWardType(cycleMonth, ward, cardType);
        if (stock == null)
            return Map.of("error", "Ward " + ward + "-এর জন্য " + cycleMonth + " চক্রে কোনো stock তৈরি হয়নি।");

        int remaining = stock.getTotalCards() - stock.getDistributed();
        if (remaining <= 0)
            return Map.of("error", "এই ward-এর stock শেষ হয়ে গেছে।");

        VgdSession session = new VgdSession();
        session.setSessionCode("VGD-" + ward + "-" + cycleMonth + "-"
                + String.format("%04d", (int)(Math.random() * 9000) + 1000));
        session.setStockId(stock.getId());
        session.setDealerName(dealerName);
        session.setWard(ward);
        session.setCycleMonth(cycleMonth);
        session.setCardType(cardType.toUpperCase());
        session.setStatus("OPEN");
        em.persist(session);
        em.flush();

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("sessionId",   session.getId());
        res.put("sessionCode", session.getSessionCode());
        res.put("cardType",    cardType);
        res.put("message",     "Session শুরু হয়েছে।");
        res.put("reused",      false);
        res.put("remaining",   remaining);
        res.put("stock",       buildStockSummary(stock));
        return res;
    }

    public Map<String, Object> closeSession(int sessionId) {
        VgdSession s = em.find(VgdSession.class, sessionId);
        if (s == null) return Map.of("error", "Session পাওয়া যায়নি।");
        s.setStatus("CLOSED");
        s.setClosedAt(LocalDateTime.now());
        em.merge(s);
        return Map.of("message", "Session বন্ধ হয়েছে। মোট বিতরণ: " + s.getTotalScanned());
    }

    public List<VgdSession> getAllSessions() {
        return em.createQuery(
                "FROM VgdSession ORDER BY openedAt DESC", VgdSession.class).getResultList();
    }

    public Map<String, Object> getSessionDetail(int sessionId) {
        VgdSession s = em.find(VgdSession.class, sessionId);
        if (s == null) return Map.of("error", "Session পাওয়া যায়নি।");

        List<Object[]> logs = em.createNativeQuery(
                "SELECT d.id, d.dist_month, d.rice_kg, d.wheat_kg, d.cash_amount, " +
                "d.received_date, d.distributed_by, c.holder_name, c.card_no, c.card_type, c.ward " +
                "FROM vgd_distribution d " +
                "JOIN vgd_card c ON d.card_id = c.id " +
                "WHERE d.session_id = :sid ORDER BY d.created_at DESC")
                .setParameter("sid", sessionId)
                .getResultList();

        return Map.of("session", s, "logs", logs, "count", logs.size());
    }

    // ══════════════════════════════════════════════════════════
    // SCAN / DISTRIBUTE
    // ══════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    public Map<String, Object> scan(int sessionId, String cardNo, String scannedBy) {
        // ── Validate session ──────────────────────────────────
        VgdSession session = em.find(VgdSession.class, sessionId);
        if (session == null)        return fail("Session পাওয়া যায়নি।");
        if ("CLOSED".equals(session.getStatus()))
            return fail("এই session ইতিমধ্যে বন্ধ হয়ে গেছে।");

        // ── Lookup card ───────────────────────────────────────
        List<VgdCard> cards = em.createQuery(
                "FROM vgdCard WHERE cardNo=:cn", VgdCard.class)
                .setParameter("cn", cardNo.trim())
                .getResultList();

        if (cards.isEmpty())  return fail("কার্ড নং '" + cardNo + "' পাওয়া যায়নি।");
        VgdCard card = cards.get(0);

        if (!"Approved".equalsIgnoreCase(card.getStatus()))
            return fail("কার্ডটি অনুমোদিত নয়। স্ট্যাটাস: " + card.getStatus());

        // ── Ward match ────────────────────────────────────────
        if (session.getWard() != null && !session.getWard().equals(card.getWard()))
            return fail("কার্ডের ward (" + card.getWard() + ") এই session-এর ward ("
                    + session.getWard() + ") এর সাথে মেলে না।");

        // ── Card type match ───────────────────────────────────
        if (!"ALL".equals(session.getCardType()) &&
                !session.getCardType().equalsIgnoreCase(card.getCardType()))
            return fail("এই session শুধুমাত্র " + session.getCardType()
                    + " কার্ডের জন্য। কার্ডটি " + card.getCardType() + " কার্ড।");

        // ── Validity check ────────────────────────────────────
        if (card.getEndDate() != null && card.getEndDate().isBefore(LocalDate.now()))
            return fail("কার্ডের মেয়াদ শেষ হয়ে গেছে: " + card.getEndDate());

        // ── Duplicate check (native SQL — no JPA entity for vgd_distribution) ──
        Long count;
        try {
            count = ((Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM vgd_distribution WHERE card_id=:id AND dist_month=:m")
                    .setParameter("id", card.getId())
                    .setParameter("m",  session.getCycleMonth())
                    .getSingleResult()).longValue();
        } catch (Exception e) { count = 0L; }
        if (count > 0)
            return fail("এই কার্ডে '" + session.getCycleMonth()
                    + "' মাসে ইতিমধ্যে বিতরণ করা হয়েছে।");

        // ── Entitlement ───────────────────────────────────────
        BigDecimal rice  = card.getMonthlyRiceKg()  != null ? card.getMonthlyRiceKg()  : BigDecimal.ZERO;
        BigDecimal wheat = card.getMonthlyWheatKg() != null ? card.getMonthlyWheatKg() : BigDecimal.ZERO;
        BigDecimal cash  = card.getCashAmount()      != null ? card.getCashAmount()     : BigDecimal.ZERO;

        // ── Stock check ───────────────────────────────────────
        if (session.getStockId() != null) {
            VgdStock stock = em.find(VgdStock.class, session.getStockId());
            if (stock != null) {
                int rem = stock.getTotalCards() - stock.getDistributed();
                if (rem <= 0) return fail("Stock batch-এর সকল কার্ডের বিতরণ সম্পন্ন হয়েছে।");
                if (rice.compareTo(BigDecimal.ZERO) > 0 && stock.getRiceKg().compareTo(rice) < 0)
                    return fail("পর্যাপ্ত চাল নেই। বাকি: " + stock.getRiceKg() + " কেজি।");
                if (cash.compareTo(BigDecimal.ZERO) > 0 && stock.getCashAmount().compareTo(cash) < 0)
                    return fail("পর্যাপ্ত নগদ নেই। বাকি: ৳" + stock.getCashAmount());
                deductStock(stock.getId(), rice, wheat, cash);
            }
        }

        // ── Insert distribution row ───────────────────────────
        em.createNativeQuery(
                "INSERT INTO vgd_distribution (card_id, dist_month, rice_kg, wheat_kg, " +
                "cash_amount, received_date, distributed_by, session_id, created_at) " +
                "VALUES (:cid,:dm,:rice,:wheat,:cash,:rd,:by,:sid,:now)")
                .setParameter("cid",  card.getId())
                .setParameter("dm",   session.getCycleMonth())
                .setParameter("rice", rice)
                .setParameter("wheat",wheat)
                .setParameter("cash", cash)
                .setParameter("rd",   LocalDate.now())
                .setParameter("by",   scannedBy)
                .setParameter("sid",  sessionId)
                .setParameter("now",  LocalDateTime.now())
                .executeUpdate();

        // Update card's lastReceivedDate
        card.setLastReceivedDate(LocalDate.now());
        em.merge(card);

        // Increment session scanned count
        session.setTotalScanned(session.getTotalScanned() + 1);
        em.merge(session);
        em.flush();

        // ── Build response ────────────────────────────────────
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("success",    true);
        res.put("holderName", card.getHolderName());
        res.put("cardNo",     card.getCardNo());
        res.put("cardType",   card.getCardType());
        res.put("ward",       card.getWard());
        res.put("riceKg",     rice);
        res.put("wheatKg",    wheat);
        res.put("cashAmount", cash);
        res.put("distDate",   LocalDate.now().toString());
        res.put("scannedAt",  LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        res.put("message",    card.getHolderName() + " কে বিতরণ সম্পন্ন ✓");
        return res;
    }

    // ══════════════════════════════════════════════════════════
    // HISTORY / REPORTS
    // ══════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    public Map<String, Object> getCycleSummary(String cycleMonth, String cardType) {
        String sql = "SELECT d.id, c.holder_name, c.card_no, c.card_type, c.ward, " +
                "d.rice_kg, d.wheat_kg, d.cash_amount, d.received_date, d.distributed_by " +
                "FROM vgd_distribution d JOIN vgd_card c ON d.card_id=c.id " +
                "WHERE d.dist_month=:m " +
                (cardType != null && !cardType.isEmpty() ? "AND c.card_type=:t " : "") +
                "ORDER BY d.received_date DESC";

        var q = em.createNativeQuery(sql).setParameter("m", cycleMonth);
        if (cardType != null && !cardType.isEmpty()) q.setParameter("t", cardType.toUpperCase());
        List<Object[]> logs = q.getResultList();

        BigDecimal totalRice  = BigDecimal.ZERO;
        BigDecimal totalCash  = BigDecimal.ZERO;
        for (Object[] r : logs) {
            if (r[5] != null) totalRice  = totalRice.add((BigDecimal) r[5]);
            if (r[7] != null) totalCash  = totalCash.add((BigDecimal) r[7]);
        }

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("cycleMonth",   cycleMonth);
        res.put("cardType",     cardType);
        res.put("totalBenef",   logs.size());
        res.put("totalRiceKg",  totalRice);
        res.put("totalCash",    totalCash);
        res.put("logs",         logs);
        return res;
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> getCardHistory(int cardId) {
        return em.createNativeQuery(
                "SELECT id, dist_month, rice_kg, wheat_kg, cash_amount, " +
                "received_date, distributed_by, remarks, session_id " +
                "FROM vgd_distribution WHERE card_id=:id ORDER BY received_date DESC")
                .setParameter("id", cardId)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> getCardHistoryByCardNo(String cardNo) {
        return em.createNativeQuery(
                "SELECT d.id, d.dist_month, d.rice_kg, d.wheat_kg, d.cash_amount, " +
                "d.received_date, d.distributed_by, c.holder_name, c.card_type " +
                "FROM vgd_distribution d JOIN vgd_card c ON d.card_id=c.id " +
                "WHERE c.card_no=:cn ORDER BY d.received_date DESC")
                .setParameter("cn", cardNo)
                .getResultList();
    }

    // ── Helpers ───────────────────────────────────────────────
    private Map<String, Object> fail(String msg) {
        return Map.of("success", false, "message", msg);
    }

    private Map<String, Object> buildStockSummary(VgdStock s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          s.getId());
        m.put("batchLabel",  s.getBatchLabel());
        m.put("ward",        s.getWard());
        m.put("cycleMonth",  s.getCycleMonth());
        m.put("cardType",    s.getCardType());
        m.put("riceKg",      s.getRiceKg());
        m.put("cashAmount",  s.getCashAmount());
        m.put("totalCards",  s.getTotalCards());
        m.put("distributed", s.getDistributed());
        m.put("remaining",   s.getTotalCards() - s.getDistributed());
        return m;
    }
}
