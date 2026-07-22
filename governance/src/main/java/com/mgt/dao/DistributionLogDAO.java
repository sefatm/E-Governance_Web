package com.mgt.dao;

import com.mgt.model.DistributionLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public class DistributionLogDAO {

    @PersistenceContext
    private EntityManager em;

    public DistributionLog save(DistributionLog log) {
        log.setScannedAt(LocalDateTime.now());
        em.persist(log);
        return log;
    }

    public boolean alreadyReceived(String cardNo, int sessionId) {
        Long count = em.createQuery(
                "select count(d) from DistributionLog d where d.cardNo = :c and d.sessionId = :s",
                Long.class)
                .setParameter("c", cardNo)
                .setParameter("s", sessionId)
                .getSingleResult();
        return count > 0;
    }

    /** Check if card received in the same cycle across ANY session */
    public boolean alreadyReceivedInCycle(String cardNo, String cycleMonth) {
        Long count = em.createQuery("""
                select count(d) from DistributionLog d
                join DistributionSession s on s.id = d.sessionId
                where d.cardNo = :c and s.cycleMonth = :cycle
                """, Long.class)
                .setParameter("c",     cardNo)
                .setParameter("cycle", cycleMonth)
                .getSingleResult();
        return count > 0;
    }

    public List<DistributionLog> getBySession(int sessionId) {
        return em.createQuery(
                "from DistributionLog where sessionId = :s order by scannedAt desc",
                DistributionLog.class)
                .setParameter("s", sessionId)
                .getResultList();
    }

    public List<DistributionLog> getByCardNo(String cardNo) {
        return em.createQuery(
                "from DistributionLog where cardNo = :c order by scannedAt desc",
                DistributionLog.class)
                .setParameter("c", cardNo)
                .getResultList();
    }
}
