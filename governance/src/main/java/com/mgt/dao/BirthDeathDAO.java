package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.BirthDeathCertificate;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "birthDeathDAO")
@Transactional
public class BirthDeathDAO {
	
	@PersistenceContext
	private  EntityManager entityManager;

	public BirthDeathCertificate save(BirthDeathCertificate birthDeath) {
        return entityManager.merge(birthDeath);
    }

    public List<BirthDeathCertificate> getall() {
        String sql = "FROM BirthDeathCertificate";
        return entityManager.createQuery(sql, BirthDeathCertificate.class)
                .getResultList();
    }

    public List<BirthDeathCertificate> findByMobile(String mobile) {
        return entityManager.createQuery(
                "FROM BirthDeathCertificate b WHERE b.mobileNumber = :mobile OR b.contact = :mobile",
                BirthDeathCertificate.class)
            .setParameter("mobile", mobile)
            .getResultList();
    }

    public BirthDeathCertificate findById(int id) {
        return entityManager.find(BirthDeathCertificate.class, id);
    }

    public void updateStatus(int id, String status) {
        BirthDeathCertificate obj = entityManager.find(BirthDeathCertificate.class, id);
        if (obj != null) {
            obj.setStatus(status);
            entityManager.merge(obj);
        }
    }
}
	
