package com.mgt.dao;

import com.mgt.model.DistributionSession;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public class DistributionSessionDAO {

    @PersistenceContext
    private EntityManager em;

    public DistributionSession save(DistributionSession s) {
        s.setOpenedAt(LocalDateTime.now());
        em.persist(s);
        return s;
    }

    public DistributionSession getById(int id) {
        return em.find(DistributionSession.class, id);
    }

    public DistributionSession getByCode(String code) {
        List<DistributionSession> res = em.createQuery(
                "from DistributionSession where sessionCode = :c", DistributionSession.class)
                .setParameter("c", code).getResultList();
        return res.isEmpty() ? null : res.get(0);
    }

    public List<DistributionSession> getAll() {
        return em.createQuery(
                "from DistributionSession order by openedAt desc", DistributionSession.class)
                .getResultList();
    }

    public List<DistributionSession> getOpenByWardAndCycle(String ward, String cycle) {
        return em.createQuery(
                "from DistributionSession where ward = :w and cycleMonth = :c and status = 'OPEN'",
                DistributionSession.class)
                .setParameter("w", ward)
                .setParameter("c", cycle)
                .getResultList();
    }

    @Transactional
    public void incrementScanned(int sessionId) {
        em.createQuery(
                "UPDATE DistributionSession s SET s.totalScanned = s.totalScanned + 1 WHERE s.id = :id")
          .setParameter("id", sessionId).executeUpdate();
    }

    @Transactional
    public void close(int sessionId) {
        em.createQuery(
                "UPDATE DistributionSession s SET s.status = 'CLOSED', s.closedAt = :now WHERE s.id = :id")
          .setParameter("now", LocalDateTime.now())
          .setParameter("id",  sessionId)
          .executeUpdate();
    }
}
