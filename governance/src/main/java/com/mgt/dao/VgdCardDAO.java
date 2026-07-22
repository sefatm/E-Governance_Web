package com.mgt.dao;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.VgdCard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "vgdCardDAO")
@Transactional
public class VgdCardDAO {

    @PersistenceContext
    private EntityManager em;

    // ── CREATE ────────────────────────────────────────────────
    public void save(VgdCard card) {
        card.setStatus("Pending");

        // ✅ FIX Bug 11: VGF cycle = 6 months (disaster relief), VGD = 24 months
        if (card.getCycleMonths() <= 0) {
            card.setCycleMonths("VGF".equalsIgnoreCase(card.getCardType()) ? 6 : 24);
        }

        // ✅ FIX: set default rice/cash per card type on create
        if ("VGD".equalsIgnoreCase(card.getCardType())) {
            if (card.getMonthlyRiceKg() == null || card.getMonthlyRiceKg().compareTo(BigDecimal.ZERO) == 0)
                card.setMonthlyRiceKg(new BigDecimal("30.00"));
        } else if ("VGF".equalsIgnoreCase(card.getCardType())) {
            card.setMonthlyRiceKg(BigDecimal.ZERO);
            if (card.getCashAmount() == null || card.getCashAmount().compareTo(BigDecimal.ZERO) == 0)
                card.setCashAmount(new BigDecimal("750.00"));
        }

        if (card.getCardNo() == null || card.getCardNo().isBlank()) {
            String prefix = "VGF".equalsIgnoreCase(card.getCardType()) ? "VGF" : "VGD";
            String ward   = card.getWard() != null ? card.getWard() : "00";
            int    seq    = (int)(Math.random() * 90000) + 10000;
            card.setCardNo(String.format("%s-%s-%d-%05d",
                    prefix, ward, LocalDateTime.now().getYear(), seq));
        }

        em.persist(card);
        em.flush(); // ✅ FIX Bug 3: flush so cardNo is immediately readable post-save
    }

    // ── READ ──────────────────────────────────────────────────
    // ✅ FIX Bug 4: ORDER BY createdAt DESC — consistent ordering
    public List<VgdCard> getAll() {
        return em.createQuery(
                "from vgdCard order by createdAt desc", VgdCard.class
        ).getResultList();
    }

    public List<VgdCard> getByStatus(String status) {
        return em.createQuery(
                "from vgdCard where status = :s order by createdAt desc",
                VgdCard.class)
                .setParameter("s", status)
                .getResultList();
    }

    public List<VgdCard> getByCardType(String cardType) {
        return em.createQuery(
                "from vgdCard where cardType = :t order by createdAt desc",
                VgdCard.class)
                .setParameter("t", cardType.toUpperCase())
                .getResultList();
    }

    public List<VgdCard> getByWard(String ward) {
        return em.createQuery(
                "from vgdCard where ward = :w order by createdAt desc",
                VgdCard.class)
                .setParameter("w", ward)
                .getResultList();
    }

    public VgdCard getById(int id) {
        return em.find(VgdCard.class, id);
    }

    public VgdCard getByNid(String nid) {
        List<VgdCard> res = em.createQuery(
                "from vgdCard where nid = :n", VgdCard.class)
                .setParameter("n", nid)
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }

    // ── EXPIRING SOON ─────────────────────────────────────────
    public List<VgdCard> getExpiringSoon(int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return em.createQuery(
                "from vgdCard where status = 'APPROVED' and endDate <= :cutoff order by endDate",
                VgdCard.class)
                .setParameter("cutoff", cutoff)
                .getResultList();
    }

    // ── UPDATE STATUS ─────────────────────────────────────────
    public void updateStatus(int id, String status, String approvedBy, String rejectionReason) {
        VgdCard card = em.find(VgdCard.class, id);
        if (card == null) return;

        card.setStatus(status);
        card.setRejectionReason(rejectionReason);

        if ("Approved".equalsIgnoreCase(status)) {
            card.setApprovedBy(approvedBy);
            card.setApprovedAt(LocalDateTime.now());

            LocalDate today = LocalDate.now();
            card.setStartDate(today);
            card.setEndDate(today.plusMonths(card.getCycleMonths()));

            // ✅ FIX Bug 5: set correct rice/cash on approval
            if ("VGD".equalsIgnoreCase(card.getCardType())) {
                // VGD: 30kg rice, no cash
                if (card.getMonthlyRiceKg() == null || card.getMonthlyRiceKg().compareTo(BigDecimal.ZERO) == 0)
                    card.setMonthlyRiceKg(new BigDecimal("30.00"));
                card.setCashAmount(BigDecimal.ZERO);
            } else if ("VGF".equalsIgnoreCase(card.getCardType())) {
                // VGF: 750 taka cash, no rice
                card.setMonthlyRiceKg(BigDecimal.ZERO);
                if (card.getCashAmount() == null || card.getCashAmount().compareTo(BigDecimal.ZERO) == 0)
                    card.setCashAmount(new BigDecimal("750.00"));
            }
        }
        em.merge(card);
    }

    /**
     * ✅ FIX Bug 6: recordDistribution previously only updated lastReceivedDate
     * on vgd_card — it never inserted a row into vgd_distribution table.
     *
     * Now it:
     *  1. Checks for duplicate distribution this month (idempotent)
     *  2. Inserts a row into vgd_distribution
     *  3. Updates lastReceivedDate on vgd_card
     */
    @SuppressWarnings("unchecked")
    public String recordDistribution(int cardId, String distMonth,
                                     String distributedBy, String remarks) {
        VgdCard card = em.find(VgdCard.class, cardId);
        if (card == null) return "NOT_FOUND";
        if (!"Approved".equalsIgnoreCase(card.getStatus()))
            return "NOT_APPROVED";

        // Duplicate check: already received this month?
        Long count;
        try {
            count = (Long) em.createNativeQuery(
                    "SELECT COUNT(*) FROM vgd_distribution WHERE card_id=:id AND dist_month=:m")
                    .setParameter("id", cardId)
                    .setParameter("m", distMonth)
                    .getSingleResult();
        } catch (Exception e) { count = 0L; }

        if (count != null && count > 0) return "ALREADY_DISTRIBUTED";

        // Insert distribution row
        em.createNativeQuery(
                "INSERT INTO vgd_distribution (card_id, dist_month, rice_kg, wheat_kg, cash_amount, " +
                "received_date, distributed_by, remarks, created_at) " +
                "VALUES (:cid, :dm, :rice, :wheat, :cash, :rd, :by, :rem, :now)")
                .setParameter("cid",   cardId)
                .setParameter("dm",    distMonth)
                .setParameter("rice",  card.getMonthlyRiceKg()  != null ? card.getMonthlyRiceKg()  : BigDecimal.ZERO)
                .setParameter("wheat", card.getMonthlyWheatKg() != null ? card.getMonthlyWheatKg() : BigDecimal.ZERO)
                .setParameter("cash",  card.getCashAmount()      != null ? card.getCashAmount()     : BigDecimal.ZERO)
                .setParameter("rd",    LocalDate.now())
                .setParameter("by",    distributedBy != null ? distributedBy : "System")
                .setParameter("rem",   remarks)
                .setParameter("now",   LocalDateTime.now())
                .executeUpdate();

        // Update lastReceivedDate on card
        card.setLastReceivedDate(LocalDate.now());
        em.merge(card);
        em.flush();

        return "OK";
    }

    // ── DISTRIBUTION HISTORY ──────────────────────────────────
    @SuppressWarnings("unchecked")
    public List<Object[]> getDistributionHistory(int cardId) {
        return em.createNativeQuery(
                "SELECT id, dist_month, rice_kg, wheat_kg, cash_amount, " +
                "received_date, distributed_by, remarks, created_at " +
                "FROM vgd_distribution WHERE card_id = :id ORDER BY received_date DESC")
                .setParameter("id", cardId)
                .getResultList();
    }

    // ── RENEWAL ──────────────────────────────────────────────
    public void renew(int id) {
        VgdCard card = em.find(VgdCard.class, id);
        if (card == null) return;
        LocalDate base = card.getEndDate() != null ? card.getEndDate() : LocalDate.now();
        card.setEndDate(base.plusMonths(card.getCycleMonths()));
        em.merge(card);
    }

    // ── DELETE ────────────────────────────────────────────────
    public void updateOnly(VgdCard card) {
        em.merge(card);
    }

    public void delete(int id) {
        em.createQuery("delete from vgdCard where id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
