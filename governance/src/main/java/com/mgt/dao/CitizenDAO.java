package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.CitizenCertificate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "citizenDAO")
@Transactional
public class CitizenDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(CitizenCertificate citizen) {
        if (citizen.getCertificateNo() == null || citizen.getCertificateNo().isBlank()) {
            citizen.setCertificateNo("CC-" + System.currentTimeMillis());
        }
        entityManager.persist(citizen);
    }

    public boolean existsByNid(String nid) {
        Long count = entityManager.createQuery(
            "SELECT COUNT(c) FROM citizen c WHERE c.nid = :nid", Long.class)
            .setParameter("nid", nid)
            .getSingleResult();
        return count != null && count > 0;
    }

    public List<CitizenCertificate> getall() {
        return entityManager.createQuery("from citizen", CitizenCertificate.class).getResultList();
    }

    public List<CitizenCertificate> getByContact(String mobile) {
        return entityManager.createQuery(
            "FROM citizen c WHERE c.contact = :mobile", CitizenCertificate.class)
            .setParameter("mobile", mobile)
            .getResultList();
    }

    public CitizenCertificate getById(int id) {
        return entityManager.find(CitizenCertificate.class, id);
    }

    public void updateStatus(int id, String status) {
        CitizenCertificate citizen = entityManager.find(CitizenCertificate.class, id);
        if (citizen != null) {
            citizen.setStatus(status);
            entityManager.merge(citizen);
        }
    }

    public void delete(int id) {
        entityManager.createQuery("delete from citizen where id = :id")
            .setParameter("id", id)
            .executeUpdate();
    }

    public void saveOrUpdate(CitizenCertificate citizen) {
        entityManager.merge(citizen);
    }
}
