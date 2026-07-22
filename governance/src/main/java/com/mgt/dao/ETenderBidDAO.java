package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.ETenderBid;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "eTenderBidDAO")
@Transactional
public class ETenderBidDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public ETenderBid save(ETenderBid bid) {
        entityManager.persist(bid);
        return bid;
    }

    public ETenderBid update(ETenderBid bid) {
        return entityManager.merge(bid);
    }

    public List<ETenderBid> getAll() {
        return entityManager
            .createQuery("FROM ETenderBid ORDER BY id DESC", ETenderBid.class)
            .getResultList();
    }

    /** Tender এর সব bids — amount ASC (lowest first) */
    public List<ETenderBid> getByTenderId(int tenderId) {
        return entityManager
            .createQuery("FROM ETenderBid WHERE tenderId = :tid ORDER BY bidAmount ASC", ETenderBid.class)
            .setParameter("tid", tenderId)
            .getResultList();
    }

    public ETenderBid getById(int id) {
        return entityManager.find(ETenderBid.class, id);
    }

    public void updateStatus(int id, String status) {
        ETenderBid bid = entityManager.find(ETenderBid.class, id);
        if (bid != null) {
            bid.setStatus(status);
            entityManager.merge(bid);
        }
    }

    public long countByTenderId(int tenderId) {
        return (long) entityManager
            .createQuery("SELECT COUNT(b) FROM ETenderBid b WHERE b.tenderId = :tid")
            .setParameter("tid", tenderId)
            .getSingleResult();
    }

    // ── Document Verification ────────────────────────────────────────────────

    /** Admin এর doc verify decision save করো */
    public void updateDocVerification(int bidId, Boolean verified, String remark) {
        ETenderBid bid = entityManager.find(ETenderBid.class, bidId);
        if (bid != null) {
            bid.setDocVerified(verified);
            bid.setDocRemark(remark);
            entityManager.merge(bid);
        }
    }

    // ── Lowest Bid Auto-Highlight ────────────────────────────────────────────

    /**
     * Tender এর সব bids এর is_lowest reset করে
     * তারপর সবচেয়ে কম amount এর bid টা is_lowest = true করে
     *
     * Call করো: bid submit হওয়ার পরে
     */
    public void recalculateLowest(int tenderId) {
        // Step 1: সবার lowest flag false করো
        entityManager.createQuery(
            "UPDATE ETenderBid SET isLowest = false WHERE tenderId = :tid")
            .setParameter("tid", tenderId)
            .executeUpdate();

        // Step 2: সবচেয়ে কম bid amount বের করো
        List<ETenderBid> bids = entityManager
            .createQuery(
                "FROM ETenderBid WHERE tenderId = :tid ORDER BY bidAmount ASC",
                ETenderBid.class)
            .setParameter("tid", tenderId)
            .setMaxResults(1)
            .getResultList();

        if (!bids.isEmpty()) {
            ETenderBid lowest = bids.get(0);
            lowest.setLowest(true);
            entityManager.merge(lowest);
        }
    }

    /** Tender এর lowest bid টা return করো */
    public ETenderBid getLowestBid(int tenderId) {
        List<ETenderBid> result = entityManager
            .createQuery("FROM ETenderBid WHERE tenderId = :tid AND isLowest = true", ETenderBid.class)
            .setParameter("tid", tenderId)
            .setMaxResults(1)
            .getResultList();
        return result.isEmpty() ? null : result.get(0);
    }
}
