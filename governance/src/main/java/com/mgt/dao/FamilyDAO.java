package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

// FIX: removed unused import com.mgt.model.BirthRegistration
import com.mgt.model.FamilyCertificate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "familyDAO")
@Transactional
public class FamilyDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(FamilyCertificate family) {
        family.setStatus("Pending");
        if (family.getCertificateNo() == null || family.getCertificateNo().isBlank()) {
            family.setCertificateNo("FAM-" + System.currentTimeMillis());
        }
        entityManager.persist(family);
    }

    public List<FamilyCertificate> getAll() {
        return entityManager.createQuery("from family", FamilyCertificate.class).getResultList();
    }

    public List<FamilyCertificate> findByContact(String mobile) {
        return entityManager.createQuery(
                "from family f where f.contact = :mobile", FamilyCertificate.class)
            .setParameter("mobile", mobile)
            .getResultList();
    }

    public FamilyCertificate getById(int id) {
        return entityManager.find(FamilyCertificate.class, id);
    }

    public void update(FamilyCertificate family) {
        entityManager.merge(family);
    }

    public void updateStatus(int id, String status) {
        FamilyCertificate family = entityManager.find(FamilyCertificate.class, id);
        if (family != null) {
            family.setStatus(status);
            entityManager.merge(family);
        }
    }

    public void delete(int id) {
        entityManager.createQuery("delete from family where id = :id")
            .setParameter("id", id)
            .executeUpdate();
    }
}
