package com.mgt.dao;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.TradeLicenseApply;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "tradelicenseDAO")
@Transactional
public class TradeLicenseDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public TradeLicenseApply save(TradeLicenseApply trade) {
        entityManager.persist(trade);
        return trade;
    }

    public TradeLicenseApply update(TradeLicenseApply trade) {
        return entityManager.merge(trade);
    }

    public List<TradeLicenseApply> getall() {
        return entityManager
            .createQuery("FROM TradeLicenseApply ORDER BY id DESC", TradeLicenseApply.class)
            .getResultList();
    }

    public TradeLicenseApply getById(int id) {
        return entityManager.find(TradeLicenseApply.class, id);
    }

    // ── Duplicate check: same NID + Pending/Approved ──────────────────────────
    public boolean existsByNidAndActiveStatus(String nid) {
        Long count = entityManager
            .createQuery(
                "SELECT COUNT(t) FROM TradeLicenseApply t " +
                "WHERE t.nid = :nid AND t.status IN ('Pending','First Approved','Approved')",
                Long.class)
            .setParameter("nid", nid)
            .getSingleResult();
        return count != null && count > 0;
    }

    public void updateStatus(int id, String status) {
        TradeLicenseApply trade = entityManager.find(TradeLicenseApply.class, id);
        if (trade != null) {
            trade.setStatus(status);
            entityManager.merge(trade);
        }
    }

    public TradeLicenseApply findByLicenseNumber(String licenseNumber) {
        try {
            return entityManager
                .createQuery("FROM TradeLicenseApply WHERE licenseNumber = :lcn",
                             TradeLicenseApply.class)
                .setParameter("lcn", licenseNumber)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public void setExpiryDate(int id, LocalDate expiry) {
        TradeLicenseApply trade = entityManager.find(TradeLicenseApply.class, id);
        if (trade != null) {
            trade.setExpiryDate(expiry);
            entityManager.merge(trade);
        }
    }

    public void updateLateFine(int id, double fine, String fineStatus) {
        TradeLicenseApply trade = entityManager.find(TradeLicenseApply.class, id);
        if (trade != null) {
        	trade.setLateFineAmount(fine);
            trade.setLateFineStatus(fineStatus);
            entityManager.merge(trade);
        }
    }

    public void markReminder60Sent(int id) {
        TradeLicenseApply t = entityManager.find(TradeLicenseApply.class, id);
        if (t != null) { t.setReminder60Sent(true); entityManager.merge(t); }
    }

    public void markReminder30Sent(int id) {
        TradeLicenseApply t = entityManager.find(TradeLicenseApply.class, id);
        if (t != null) { t.setReminder30Sent(true); entityManager.merge(t); }
    }

    public List<TradeLicenseApply> getExpiringIn60DaysNotReminded() {
        LocalDate from = LocalDate.now().plusDays(55);
        LocalDate to   = LocalDate.now().plusDays(65);
        return entityManager.createQuery(
            "FROM TradeLicenseApply WHERE status='Approved' " +
            "AND expiryDate BETWEEN :from AND :to AND reminder60Sent = false",
            TradeLicenseApply.class)
            .setParameter("from", from).setParameter("to", to)
            .getResultList();
    }

    public List<TradeLicenseApply> getExpiringIn30DaysNotReminded() {
        LocalDate from = LocalDate.now().plusDays(25);
        LocalDate to   = LocalDate.now().plusDays(35);
        return entityManager.createQuery(
            "FROM TradeLicenseApply WHERE status='Approved' " +
            "AND expiryDate BETWEEN :from AND :to AND reminder30Sent = false",
            TradeLicenseApply.class)
            .setParameter("from", from).setParameter("to", to)
            .getResultList();
    }
}
