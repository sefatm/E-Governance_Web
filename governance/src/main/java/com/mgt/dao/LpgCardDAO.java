package com.mgt.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.LpgCard;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "lpgCardDAO")
@Transactional
public class LpgCardDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(LpgCard card) {
        card.setStatus("Pending");
        if (card.getCardNo() == null || card.getCardNo().isBlank()) {
            card.setCardNo("LPG-" + LocalDateTime.now().getYear()
                    + "-" + String.format("%05d", (int)(Math.random() * 90000) + 10000));
        }
        if (card.getMembersCount() > 6) {
            card.setMonthlyQuota(2);
        }
        entityManager.persist(card);
    }

    public List<LpgCard> getAll() {
        return entityManager.createQuery("from lpgCard", LpgCard.class).getResultList();
    }

    public List<LpgCard> getByStatus(String status) {
        return entityManager.createQuery(
                "from lpgCard where status = :status", LpgCard.class)
                .setParameter("status", status)
                .getResultList();
    }

    public List<LpgCard> getByDealer(String dealerCode) {
        return entityManager.createQuery(
                "from lpgCard where dealerCode = :dealerCode", LpgCard.class)
                .setParameter("dealerCode", dealerCode)
                .getResultList();
    }

    public List<LpgCard> getByDistrict(String district) {
        return entityManager.createQuery(
                "from lpgCard where district = :district", LpgCard.class)
                .setParameter("district", district)
                .getResultList();
    }

    public LpgCard getById(int id) {
        return entityManager.find(LpgCard.class, id);
    }

    public LpgCard getByNid(String nid) {
        List<LpgCard> result = entityManager
                .createQuery("from lpgCard where nid = :nid", LpgCard.class)
                .setParameter("nid", nid)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    public void updateStatus(int id, String status, String approvedBy, String rejectionReason) {
        LpgCard card = entityManager.find(LpgCard.class, id);
        if (card != null) {
            card.setStatus(status);
            card.setRejectionReason(rejectionReason);
            if ("Approved".equalsIgnoreCase(status)) {
                card.setApprovedBy(approvedBy);
                card.setApprovedAt(LocalDateTime.now());
            }
            entityManager.merge(card);
        }
    }
    
    public LpgCard getByCardNo(String cardNo) {
        List<LpgCard> result = entityManager
                .createQuery("from lpgCard where cardNo = :cardNo", LpgCard.class)
                .setParameter("cardNo", cardNo)
                .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    public void recordCollection(int id) {
        LpgCard card = entityManager.find(LpgCard.class, id);
        if (card != null) {
            card.setLastCollectedAt(java.time.LocalDate.now());
            entityManager.merge(card);
        }
    }

    public void updateOnly(LpgCard card) {
        entityManager.merge(card);
    }

    public void delete(int id) {
        entityManager.createQuery("delete from lpgCard where id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
