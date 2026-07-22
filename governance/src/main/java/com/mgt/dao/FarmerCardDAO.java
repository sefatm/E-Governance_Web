package com.mgt.dao;

import com.mgt.model.FarmerCard;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository(value = "farmerCardDAO")
@Transactional
public class FarmerCardDAO {

    @PersistenceContext
    private EntityManager em;

    // ── CREATE ────────────────────────────────────────────────
    public void create(FarmerCard card) {
        card.setStatus("Pending");

        if (card.getCardNo() == null || card.getCardNo().isBlank()) {
            String ward = card.getWard() != null ? card.getWard() : "00";
            int seq = (int)(Math.random() * 90000) + 10000;
            card.setCardNo(String.format("FRM-%s-%d-%05d",
                    ward, LocalDateTime.now().getYear(), seq));
        }
        em.persist(card);
        em.flush();
    }

    // ── UPDATE (no status reset) ──────────────────────────────
    public void updateOnly(FarmerCard card) {
        em.merge(card);
    }

    // ── READ ──────────────────────────────────────────────────
    public List<FarmerCard> getAll() {
        return em.createQuery(
                "from farmerCard order by createdAt desc", FarmerCard.class
        ).getResultList();
    }

    public List<FarmerCard> getByStatus(String status) {
        return em.createQuery(
                "from farmerCard where status = :s order by createdAt desc",
                FarmerCard.class)
                .setParameter("s", status)
                .getResultList();
    }

    public List<FarmerCard> getByDistrict(String district) {
        return em.createQuery(
                "from farmerCard where district = :d order by createdAt desc",
                FarmerCard.class)
                .setParameter("d", district)
                .getResultList();
    }

    public List<FarmerCard> getByWard(String ward) {
        return em.createQuery(
                "from farmerCard where ward = :w order by createdAt desc",
                FarmerCard.class)
                .setParameter("w", ward)
                .getResultList();
    }

    public FarmerCard getById(int id) {
        return em.find(FarmerCard.class, id);
    }

    public FarmerCard getByNid(String nid) {
        List<FarmerCard> res = em.createQuery(
                "from farmerCard where nid = :n", FarmerCard.class)
                .setParameter("n", nid)
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }
    
    public FarmerCard getByCardNo(String cardNo) {
        List<FarmerCard> res = em.createQuery(
                "from farmerCard where cardNo = :c", FarmerCard.class)
                .setParameter("c", cardNo)
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }

    // ── DUPLICATE DETECTION ───────────────────────────────────
    /**
     * Check if NID already exists in any other card table
     * Used before approve to warn admin of cross-card duplicates
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> checkDuplicateAcrossCards(String nid) {
        String sql = """
            SELECT 'FamilyCard' as card_type, card_no, holder_name, status FROM family_card WHERE nid = :nid
            UNION ALL
            SELECT 'LpgCard',   card_no, holder_name, status FROM lpg_card   WHERE nid = :nid
            UNION ALL
            SELECT 'VgdCard',   card_no, holder_name, status FROM vgd_card   WHERE nid = :nid
            """;
        return em.createNativeQuery(sql)
                .setParameter("nid", nid)
                .getResultList();
    }

    // ── STATUS UPDATE ─────────────────────────────────────────
    public void updateStatus(int id, String status, String approvedBy, String rejectionReason) {
        FarmerCard card = em.find(FarmerCard.class, id);
        if (card == null) return;

        card.setStatus(status);
        card.setRejectionReason(rejectionReason);

        if ("Approved".equalsIgnoreCase(status)) {
            card.setApprovedBy(approvedBy);
            card.setApprovedAt(LocalDateTime.now());
            // Auto-set expire date: 1 year from approval
            card.setExpireDate(LocalDate.now().plusYears(1));
            // Auto-calculate subsidy quota if not already set
            if (card.getFertilizerQuota() == null || card.getFertilizerQuota().doubleValue() == 0) {
                double acres = card.getLandTotal().doubleValue();
                card.setFertilizerQuota(new java.math.BigDecimal(Math.round(acres * 20)));
                card.setSeedQuota(new java.math.BigDecimal(Math.round(acres * 5)));
            }
        }
        em.merge(card);
    }

    // ── LAND VERIFICATION ─────────────────────────────────────
    public void verifyLand(int id, String verifiedBy) {
        FarmerCard card = em.find(FarmerCard.class, id);
        if (card == null) return;
        card.setLandVerified(true);
        card.setLandVerifiedBy(verifiedBy);
        card.setLandVerifiedAt(LocalDateTime.now());
        em.merge(card);
    }

    public void unverifyLand(int id) {
        FarmerCard card = em.find(FarmerCard.class, id);
        if (card == null) return;
        card.setLandVerified(false);
        card.setLandVerifiedBy(null);
        card.setLandVerifiedAt(null);
        em.merge(card);
    }

    // ── OFFICER ASSIGNMENT ────────────────────────────────────
    public void assignOfficer(int id, String officerName) {
        FarmerCard card = em.find(FarmerCard.class, id);
        if (card == null) return;
        card.setAssignedOfficer(officerName);
        em.merge(card);
    }

    // ── RENEWAL ──────────────────────────────────────────────
    public void renew(int id) {
        FarmerCard card = em.find(FarmerCard.class, id);
        if (card == null) return;
        LocalDate base = card.getExpireDate() != null
                ? card.getExpireDate()
                : LocalDate.now();
        card.setExpireDate(base.plusYears(1));
        card.setRenewalStatus("Renewed");
        em.merge(card);
    }

    // ── EXPIRING SOON ─────────────────────────────────────────
    public List<FarmerCard> getExpiringSoon(int days) {
        LocalDate cutoff = LocalDate.now().plusDays(days);
        return em.createQuery(
                "from farmerCard where status = 'Approved' and expireDate <= :cutoff order by expireDate",
                FarmerCard.class)
                .setParameter("cutoff", cutoff)
                .getResultList();
    }

    // ── BULK APPROVE ──────────────────────────────────────────
    public int bulkApproveByWard(String ward, String approvedBy) {
        List<FarmerCard> pending = em.createQuery(
                "from farmerCard where status = 'Pending' and ward = :w",
                FarmerCard.class)
                .setParameter("w", ward)
                .getResultList();

        int count = 0;
        for (FarmerCard c : pending) {
            updateStatus(c.getId(), "Approved", approvedBy, null);
            count++;
        }
        return count;
    }

    // ── DELETE ────────────────────────────────────────────────
    public void delete(int id) {
        em.createQuery("delete from farmerCard where id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    public EntityManager getEm() { return em; }
}
