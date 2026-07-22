package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.PaymentReceipt;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "paymentReceiptDAO")
@Transactional
public class PaymentReceiptDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public PaymentReceipt save(PaymentReceipt receipt) {
        entityManager.persist(receipt);
        return receipt;
    }

    public List<PaymentReceipt> getAll() {
        return entityManager
            .createQuery("from PaymentReceipt order by id desc", PaymentReceipt.class)
            .getResultList();
    }

    public PaymentReceipt getById(int id) {
        return entityManager.find(PaymentReceipt.class, id);
    }

    public PaymentReceipt getByTxnId(int txnId) {
        try {
            return entityManager
                .createQuery("from PaymentReceipt where txnId = :tid", PaymentReceipt.class)
                .setParameter("tid", txnId)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<PaymentReceipt> getByCitizenNid(String nid) {
        return entityManager
            .createQuery("from PaymentReceipt where citizenNid = :nid order by id desc",
                         PaymentReceipt.class)
            .setParameter("nid", nid)
            .getResultList();
    }

    public PaymentReceipt getByReceiptNo(String receiptNo) {
        try {
            return entityManager
                .createQuery("from PaymentReceipt where receiptNo = :rn", PaymentReceipt.class)
                .setParameter("rn", receiptNo)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
