package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.TradeRenewal;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "tradeRenewalDAO")
@Transactional
public class TradeRenewalDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public TradeRenewal save(TradeRenewal renewal) {
        entityManager.persist(renewal);
        return renewal;
    }

    public TradeRenewal update(TradeRenewal renewal) {
        return entityManager.merge(renewal);
    }

    public List<TradeRenewal> getall() {
        return entityManager
            .createQuery("FROM TradeRenewal ORDER BY id DESC", TradeRenewal.class)
            .getResultList();
    }

    public TradeRenewal getById(int id) {
        return entityManager.createQuery(
            "SELECT r FROM TradeRenewal r LEFT JOIN FETCH r.originalLicense WHERE r.id = :id",
            TradeRenewal.class
        )
        .setParameter("id", id)
        .getSingleResult();
    }

    public void updateStatus(int id, String status) {
        TradeRenewal renewal = entityManager.find(TradeRenewal.class, id);
        if (renewal != null) {
            renewal.setStatus(status);
            entityManager.merge(renewal);
        }
    }

    public TradeRenewal findByLicenseNumber(String licenseNumber) {
        try {
            return entityManager
                .createQuery("FROM TradeRenewal WHERE originalLicense.licenseNumber = :lcn", TradeRenewal.class)
                .setParameter("lcn", licenseNumber)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
