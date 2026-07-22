package com.mgt.dao;

import com.mgt.model.LpgDistributionLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Transactional
public class LpgDistributionDAO {

    @PersistenceContext
    private EntityManager em;

    public LpgDistributionLog save(LpgDistributionLog log) {
        em.persist(log);
        em.flush();
        return log;
    }

    /** এই চক্রে এই কার্ডে ইতিমধ্যে বিতরণ হয়েছে? */
    public boolean alreadyCollectedThisCycle(int cardId, String cycleMonth) {
        Long count = em.createQuery(
                "SELECT COUNT(l) FROM LpgDistributionLog l WHERE l.cardId = :id AND l.cycleMonth = :c",
                Long.class)
                .setParameter("id", cardId)
                .setParameter("c",  cycleMonth)
                .getSingleResult();
        return count > 0;
    }

    /** একটি কার্ডের পূর্ণ ইতিহাস */
    public List<LpgDistributionLog> getByCardId(int cardId) {
        return em.createQuery(
                "FROM LpgDistributionLog WHERE cardId = :id ORDER BY createdAt DESC",
                LpgDistributionLog.class)
                .setParameter("id", cardId)
                .getResultList();
    }

    /** কার্ড নম্বর দিয়ে ইতিহাস (Angular lookup-এর জন্য) */
    public List<LpgDistributionLog> getByCardNo(String cardNo) {
        return em.createQuery(
                "FROM LpgDistributionLog WHERE cardNo = :c ORDER BY createdAt DESC",
                LpgDistributionLog.class)
                .setParameter("c", cardNo)
                .getResultList();
    }

    /** নির্দিষ্ট চক্রের সব বিতরণ */
    public List<LpgDistributionLog> getByCycle(String cycleMonth) {
        return em.createQuery(
                "FROM LpgDistributionLog WHERE cycleMonth = :c ORDER BY createdAt DESC",
                LpgDistributionLog.class)
                .setParameter("c", cycleMonth)
                .getResultList();
    }

    /** নির্দিষ্ট চক্র + ডিলারের বিতরণ */
    public List<LpgDistributionLog> getByCycleAndDealer(String cycleMonth, String dealerCode) {
        return em.createQuery(
                "FROM LpgDistributionLog WHERE cycleMonth = :c AND dealerCode = :d ORDER BY createdAt DESC",
                LpgDistributionLog.class)
                .setParameter("c", cycleMonth)
                .setParameter("d", dealerCode)
                .getResultList();
    }
}
