package com.mgt.dao;

import com.mgt.model.LpgStock;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public class LpgStockDAO {

    @PersistenceContext
    private EntityManager em;

    public LpgStock save(LpgStock s) {
        if (s.getDistributed() < 0) s.setDistributed(0);
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        em.persist(s);
        em.flush();
        return s;
    }

    public LpgStock getById(int id) {
        return em.find(LpgStock.class, id);
    }

    public List<LpgStock> getAll() {
        return em.createQuery(
                "FROM LpgStock ORDER BY cycleMonth DESC, ward",
                LpgStock.class).getResultList();
    }

    public List<LpgStock> getByCycle(String cycleMonth) {
        return em.createQuery(
                "FROM LpgStock WHERE cycleMonth = :c ORDER BY createdAt DESC",
                LpgStock.class)
                .setParameter("c", cycleMonth)
                .getResultList();
    }

    public LpgStock getByCycleAndWard(String cycleMonth, String ward) {
        List<LpgStock> res = em.createQuery(
                "FROM LpgStock WHERE cycleMonth = :c AND ward = :w",
                LpgStock.class)
                .setParameter("c", cycleMonth)
                .setParameter("w", ward)
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }

    public LpgStock getLatestByCycle(String cycleMonth) {
        List<LpgStock> res = em.createQuery(
                "FROM LpgStock WHERE cycleMonth = :c ORDER BY createdAt DESC",
                LpgStock.class)
                .setParameter("c", cycleMonth)
                .setMaxResults(1)
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }

    /**
     * সিলিন্ডার বিতরণের সময় stock থেকে কাটা।
     * TcbStockDAO.deductStock() এর মতো JPQL UPDATE।
     *
     * @param stockId     কোন stock row থেকে কাটবে
     * @param qty         কতটা সিলিন্ডার কাটবে
     */
    @Transactional
    public void deductStock(int stockId, int qty) {
        em.createQuery("""
            UPDATE LpgStock s SET
              s.distributed = COALESCE(s.distributed, 0) + :qty,
              s.updatedAt   = :now
            WHERE s.id = :id
            """)
          .setParameter("qty",  qty)
          .setParameter("now",  LocalDateTime.now())
          .setParameter("id",   stockId)
          .executeUpdate();
    }

    public void delete(int id) {
        LpgStock s = em.find(LpgStock.class, id);
        if (s != null) em.remove(s);
    }
}
