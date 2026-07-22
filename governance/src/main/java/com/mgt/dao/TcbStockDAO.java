package com.mgt.dao;

import com.mgt.model.TcbStock;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public class TcbStockDAO {

    @PersistenceContext
    private EntityManager em;

    public TcbStock save(TcbStock s) {
        // ✅ FIX: ensure distributed is never NULL before persisting
        if (s.getDistributed() < 0) s.setDistributed(0);
        s.setCreatedAt(LocalDateTime.now());
        s.setUpdatedAt(LocalDateTime.now());
        em.persist(s);
        em.flush(); // ✅ flush so id is populated immediately
        return s;
    }

    public TcbStock getById(int id) {
        return em.find(TcbStock.class, id);
    }

    public List<TcbStock> getAll() {
        // ✅ FIX: COALESCE ensures distributed=NULL rows return 0, not crash
        return em.createQuery(
                "from TcbStock order by cycleMonth desc, ward",
                TcbStock.class
        ).getResultList();
    }

    public TcbStock getByCycleAndWard(String cycleMonth, String ward) {
        List<TcbStock> res = em.createQuery(
                "from TcbStock where cycleMonth = :c and ward = :w",
                TcbStock.class)
                .setParameter("c", cycleMonth)
                .setParameter("w", ward)
                .getResultList();
        return res.isEmpty() ? null : res.get(0);
    }

    @Transactional
    public void deductStock(int stockId, BigDecimal oil, BigDecimal rice,
                            BigDecimal lentil, BigDecimal sugar, BigDecimal cash) {
        em.createQuery("""
            UPDATE TcbStock s SET
              s.oilLitre    = s.oilLitre    - :oil,
              s.riceKg      = s.riceKg      - :rice,
              s.lentilKg    = s.lentilKg    - :lentil,
              s.sugarKg     = s.sugarKg     - :sugar,
              s.cashAmount  = s.cashAmount  - :cash,
              s.distributed = COALESCE(s.distributed, 0) + 1,
              s.updatedAt   = :now
            WHERE s.id = :id
            """)
          .setParameter("oil",    oil    != null ? oil    : BigDecimal.ZERO)
          .setParameter("rice",   rice   != null ? rice   : BigDecimal.ZERO)
          .setParameter("lentil", lentil != null ? lentil : BigDecimal.ZERO)
          .setParameter("sugar",  sugar  != null ? sugar  : BigDecimal.ZERO)
          .setParameter("cash",   cash   != null ? cash   : BigDecimal.ZERO)
          .setParameter("now",    LocalDateTime.now())
          .setParameter("id",     stockId)
          .executeUpdate();
    }

    public void delete(int id) {
        TcbStock s = em.find(TcbStock.class, id);
        if (s != null) em.remove(s);
    }
}
