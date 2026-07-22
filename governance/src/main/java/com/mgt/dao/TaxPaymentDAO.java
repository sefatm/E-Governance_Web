package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.TaxPayment;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "taxPaymentDAO")
@Transactional
public class TaxPaymentDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(TaxPayment payment) {
        entityManager.persist(payment);
    }

    public List<TaxPayment> getAll() {
        return entityManager
            .createQuery("from TaxPayment order by createdAt desc", TaxPayment.class)
            .getResultList();
    }

    public TaxPayment getById(int id) {
        return entityManager.find(TaxPayment.class, id);
    }

    public List<TaxPayment> getByHoldingNo(String holdingNo) {
        return entityManager
            .createQuery("from TaxPayment t where t.holdingNo = :h order by createdAt desc", TaxPayment.class)
            .setParameter("h", holdingNo)
            .getResultList();
    }

    public Double getTotalPaidByHoldingNo(String holdingNo) {
        Double total = (Double) entityManager
            .createQuery("select sum(t.amount) from TaxPayment t where t.holdingNo = :h and t.status = 'Paid'")
            .setParameter("h", holdingNo)
            .getSingleResult();
        return total != null ? total : 0.0;
    }

    public void updateStatus(int id, String status) {
        TaxPayment p = entityManager.find(TaxPayment.class, id);
        if (p != null) {
            p.setStatus(status);
            entityManager.merge(p);
        }
    }

    public void delete(int id) {
        entityManager.createQuery("delete from TaxPayment t where t.id = :id")
            .setParameter("id", id).executeUpdate();
    }
}
