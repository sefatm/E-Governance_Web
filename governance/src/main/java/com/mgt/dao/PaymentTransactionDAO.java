package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.PaymentTransaction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "paymentTransactionDAO")
@Transactional
public class PaymentTransactionDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public PaymentTransaction save(PaymentTransaction txn) {
        entityManager.persist(txn);
        return txn;
    }

    public List<PaymentTransaction> getAll() {
        return entityManager
            .createQuery("from PaymentTransaction order by id desc", PaymentTransaction.class)
            .getResultList();
    }

    public PaymentTransaction getById(int id) {
        return entityManager.find(PaymentTransaction.class, id);
    }

    public List<PaymentTransaction> getByCitizenNid(String nid) {
        return entityManager
            .createQuery("from PaymentTransaction where citizenNid = :nid order by id desc",
                         PaymentTransaction.class)
            .setParameter("nid", nid)
            .getResultList();
    }

    public List<PaymentTransaction> getByStatus(String status) {
        return entityManager
            .createQuery("from PaymentTransaction where status = :s order by id desc",
                         PaymentTransaction.class)
            .setParameter("s", status)
            .getResultList();
    }

    public void updateStatus(int id, String status) {
        PaymentTransaction txn = entityManager.find(PaymentTransaction.class, id);
        if (txn != null) {
            txn.setStatus(status);
            entityManager.merge(txn);
        }
    }

    public PaymentTransaction update(PaymentTransaction txn) {
        return entityManager.merge(txn);
    }

    public double getTotalCollected() {
        Object result = entityManager
            .createQuery("select sum(t.amount) from PaymentTransaction t where t.status = 'Completed'")
            .getSingleResult();
        return result == null ? 0.0 : (double) result;
    }

    public long countByStatus(String status) {
        return (long) entityManager
            .createQuery("select count(t) from PaymentTransaction t where t.status = :s")
            .setParameter("s", status)
            .getSingleResult();
    }
}
