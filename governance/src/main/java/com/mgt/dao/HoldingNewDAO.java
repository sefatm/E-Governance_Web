package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.HoldingNewRegistration;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;


@Repository(value = "holdingNewDAO")
@Transactional
public class HoldingNewDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(HoldingNewRegistration holding) {
        entityManager.persist(holding);
    }

    public List<HoldingNewRegistration> getall() {
        return entityManager
                .createQuery("from HoldingNewRegistration", HoldingNewRegistration.class)
                .getResultList();
    }

    public HoldingNewRegistration getById(int id) {
        return entityManager.find(HoldingNewRegistration.class, id);
    }

    public void updateStatus(int id, String status) {
        HoldingNewRegistration h = entityManager.find(HoldingNewRegistration.class, id);
        if (h != null) {
            h.setStatus(status);
            entityManager.merge(h);
        }
    }

    public void update(HoldingNewRegistration h) {
        entityManager.merge(h);
    }

    public void updateLocation(int id, double lat, double lng) {
        HoldingNewRegistration h = entityManager.find(HoldingNewRegistration.class, id);
        if (h != null) {
            h.setLatitude(lat);
            h.setLongitude(lng);
            entityManager.merge(h);
        }
    }
}
