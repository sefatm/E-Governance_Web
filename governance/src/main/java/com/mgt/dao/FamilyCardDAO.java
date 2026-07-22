package com.mgt.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.FamilyCard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "familyCardDAO")
@Transactional
public class FamilyCardDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(FamilyCard card) {
        card.setStatus("Pending");
        if (card.getCardNo() == null || card.getCardNo().isBlank()) {
            // ✅ FIX: Format was "FC-YYYY-NNNNN" but querySingleCard in TcbDistributionService
            // searches by card_no directly — QR on PDF encodes "CARD:FC-...|NID:..."
            // and TcbScanComponent strips the prefix before sending cardNo.
            // Standardise to "FAM-WW-YYYY-NNNNN" to match all 4 card types' convention
            // (FarmerCardDAO uses FRM-, LpgCardDAO uses LPG-, VgdCardDAO uses VGD-)
            String ward = card.getWard() != null ? card.getWard() : "00";
            int seq = (int)(Math.random() * 90000) + 10000;
            card.setCardNo(String.format("FAM-%s-%d-%05d",
                    ward, LocalDateTime.now().getYear(), seq));
        }
        entityManager.persist(card);
        entityManager.flush(); // ✅ flush so cardNo is immediately readable post-save
    }

    public List<FamilyCard> getAll() {
        return entityManager.createQuery(
                "from familyCard order by createdAt desc", FamilyCard.class)
                .getResultList();
    }

    public List<FamilyCard> getByStatus(String status) {
        return entityManager.createQuery(
                "from familyCard where status = :status order by createdAt desc",
                FamilyCard.class)
                .setParameter("status", status)
                .getResultList();
    }

    public FamilyCard getById(int id) {
        return entityManager.find(FamilyCard.class, id);
    }

    public FamilyCard getByNid(String nid) {
        List<FamilyCard> result = entityManager
                .createQuery("from familyCard where nid = :nid", FamilyCard.class)
                .setParameter("nid", nid)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    // ✅ FIX: added null guard — prevents NPE if card not found
    public void updateStatus(int id, String status, String approvedBy, String rejectionReason) {
        FamilyCard card = entityManager.find(FamilyCard.class, id);
        if (card == null) return;

        card.setStatus(status);
        card.setRejectionReason(rejectionReason);

        if ("Approved".equalsIgnoreCase(status)) {
            card.setApprovedBy(approvedBy);
            card.setApprovedAt(LocalDateTime.now());
        }
        entityManager.merge(card);
    }

    public void updateOnly(FamilyCard card) {
        entityManager.merge(card);
    }

    public void delete(int id) {
        entityManager.createQuery("delete from familyCard where id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

    public jakarta.persistence.EntityManager getEm() {
        return entityManager;
    }
}
