package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.PassportApply;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "passportDAO")
@Transactional
public class PassportDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(PassportApply passport) {
        entityManager.persist(passport);
    }

    public List<PassportApply> getall() {
        String sql = "from passport";
        List<PassportApply> passports = entityManager.createQuery(sql).getResultList();
        return passports;
    }

    public List<PassportApply> findByContact(String mobile) {
        return entityManager.createQuery(
                "from passport p where p.contact = :mobile", PassportApply.class)
            .setParameter("mobile", mobile)
            .getResultList();
    }

    public void updateStatus(int id, String status) {
        PassportApply passport = entityManager.find(PassportApply.class, id);
        if (passport != null) {
            passport.setStatus(status);   // FIX: correct setter
            entityManager.merge(passport);
        }
    }

    public void approve(int id) {
        PassportApply passport = entityManager.find(PassportApply.class, id);
        if (passport != null) {
            passport.setStatus("APPROVED");
            passport.setRejectReason(null);
            entityManager.merge(passport);
        }
    }

    public void reject(int id, String reason) {
        PassportApply passport = entityManager.find(PassportApply.class, id);
        if (passport != null) {
            passport.setStatus("REJECTED");
            passport.setRejectReason(reason);
            entityManager.merge(passport);
        }
    }

    public void update(PassportApply passport) {
        entityManager.merge(passport);
    }

    public void updateFiles(int id, String photoUrl, String nidFileUrl, String birthFileUrl) {
        entityManager.createQuery(
            "UPDATE passport p SET " +
            "p.photoUrl = :photoUrl, " +
            "p.nidFileUrl = :nidFileUrl, " +
            "p.birthFileUrl = :birthFileUrl " +
            "WHERE p.id = :id")
            .setParameter("photoUrl",    photoUrl)
            .setParameter("nidFileUrl",  nidFileUrl)
            .setParameter("birthFileUrl",birthFileUrl)
            .setParameter("id", id)
            .executeUpdate();
    }

    public void delete(int id) {
        entityManager.createQuery("delete from passport where id = :id")
            .setParameter("id", id)
            .executeUpdate();
    }
}
