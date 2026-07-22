package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.TradeInspection;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class TradeInspectionDAO {

    @PersistenceContext
    private EntityManager em;

    public TradeInspection save(TradeInspection inspection) {
        em.persist(inspection);
        return inspection;
    }

    public TradeInspection update(TradeInspection inspection) {
        return em.merge(inspection);
    }

    public TradeInspection getById(int id) {
        return em.find(TradeInspection.class, id);
    }

    /** সব inspections */
    public List<TradeInspection> getAll() {
        return em.createQuery("FROM TradeInspection ORDER BY inspectionDate DESC", TradeInspection.class)
                 .getResultList();
    }

    /** Specific license এর inspections */
    public List<TradeInspection> getByLicenseId(int licenseId) {
        return em.createQuery(
            "FROM TradeInspection WHERE licenseId = :lid ORDER BY scheduledAt DESC",
            TradeInspection.class)
            .setParameter("lid", licenseId)
            .getResultList();
    }

    /** Scheduled inspections — আজকের জন্য */
    public List<TradeInspection> getTodaysInspections() {
        return em.createQuery(
            "FROM TradeInspection WHERE inspectionDate = CURRENT_DATE AND status = 'Scheduled'",
            TradeInspection.class)
            .getResultList();
    }

    /** Status দিয়ে filter */
    public List<TradeInspection> getByStatus(String status) {
        return em.createQuery(
            "FROM TradeInspection WHERE status = :s ORDER BY inspectionDate ASC",
            TradeInspection.class)
            .setParameter("s", status)
            .getResultList();
    }

    /** একটা license এর latest inspection */
    public TradeInspection getLatestByLicenseId(int licenseId) {
        List<TradeInspection> list = em.createQuery(
            "FROM TradeInspection WHERE licenseId = :lid ORDER BY scheduledAt DESC",
            TradeInspection.class)
            .setParameter("lid", licenseId)
            .setMaxResults(1)
            .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public void delete(int id) {
        TradeInspection t = em.find(TradeInspection.class, id);
        if (t != null) em.remove(t);
    }
}
