package com.mgt.dao;

import com.mgt.model.FarmerStock;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * FarmerStock DAO — TcbStockDAO এর মতো প্যাটার্ন follow করে।
 */
@Repository
@Transactional
public class FarmerStockDAO {

    @PersistenceContext
    private EntityManager em;

    /** নতুন স্টক এন্ট্রি সেভ করুন */
    public FarmerStock save(FarmerStock s) {
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        em.persist(s);
        em.flush(); // id তাৎক্ষণিক populate হবে
        return s;
    }

    public FarmerStock getById(int id) {
        return em.find(FarmerStock.class, id);
    }

    /** সব স্টক — নতুন থেকে পুরনো */
    public List<FarmerStock> getAll() {
        return em.createQuery(
                "FROM FarmerStock ORDER BY cycleMonth DESC",
                FarmerStock.class
        ).getResultList();
    }

    /** নির্দিষ্ট চক্রের স্টক */
    public List<FarmerStock> getByCycle(String cycleMonth) {
        return em.createQuery(
                "FROM FarmerStock WHERE cycleMonth = :c ORDER BY createdAt DESC",
                FarmerStock.class)
                .setParameter("c", cycleMonth)
                .getResultList();
    }

    /** নির্দিষ্ট চক্র ও ওয়ার্ডের স্টক (যদি ward ভিত্তিক হয়) */
    public FarmerStock getByCycleAndWard(String cycleMonth, String ward) {
        List<FarmerStock> res = em.createQuery(
                "FROM FarmerStock WHERE cycleMonth = :c AND ward = :w",
                FarmerStock.class)
                .setParameter("c", cycleMonth)
                .setParameter("w", ward)
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }

    /**
     * বিতরণের সময় স্টক কমানো — TcbStockDAO.deductStock() এর মতো।
     * distribute() service method-এ call হবে।
     *
     * @param stockId       কোন stock row থেকে কাটবে
     * @param fertilizerKg  কত kg সার কাটবে
     * @param seedKg        কত kg বীজ কাটবে
     */
    @Transactional
    public void deductStock(int stockId, BigDecimal fertilizerKg, BigDecimal seedKg) {
        em.createQuery("""
            UPDATE FarmerStock s SET
              s.fertilizerDistributed = s.fertilizerDistributed + :fert,
              s.seedDistributed       = s.seedDistributed       + :seed,
              s.updatedAt             = :now
            WHERE s.id = :id
            """)
          .setParameter("fert", fertilizerKg != null ? fertilizerKg : BigDecimal.ZERO)
          .setParameter("seed", seedKg       != null ? seedKg       : BigDecimal.ZERO)
          .setParameter("now",  LocalDateTime.now())
          .setParameter("id",   stockId)
          .executeUpdate();
    }

    /**
     * চক্রের সবচেয়ে সাম্প্রতিক stock খুঁজে বের করা।
     * distribute() service-এ কোন stock row থেকে কাটবে সেটা ঠিক করতে।
     */
    public FarmerStock getLatestByCycle(String cycleMonth) {
        List<FarmerStock> res = em.createQuery(
                "FROM FarmerStock WHERE cycleMonth = :c ORDER BY createdAt DESC",
                FarmerStock.class)
                .setParameter("c", cycleMonth)
                .setMaxResults(1)
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }

    public void delete(int id) {
        FarmerStock s = em.find(FarmerStock.class, id);
        if (s != null) em.remove(s);
    }
}
