package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.TaxAssessment;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "taxAssessmentDAO")
@Transactional
public class TaxAssessmentDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(TaxAssessment assessment) {
        entityManager.persist(assessment);
    }

    public List<TaxAssessment> getAll() {
        return entityManager
            .createQuery("from TaxAssessment order by createdAt desc", TaxAssessment.class)
            .getResultList();
    }

    public TaxAssessment getById(int id) {
        return entityManager.find(TaxAssessment.class, id);
    }

    public List<TaxAssessment> getByHoldingNo(String holdingNo) {
        return entityManager
            .createQuery("from TaxAssessment t where t.holdingNo = :h order by createdAt desc", TaxAssessment.class)
            .setParameter("h", holdingNo)
            .getResultList();
    }

    public void updateStatus(int id, String status) {
        TaxAssessment a = entityManager.find(TaxAssessment.class, id);
        if (a != null) {
            a.setStatus(status);
            entityManager.merge(a);
        }
    }

    public void delete(int id) {
        entityManager.createQuery("delete from TaxAssessment t where t.id = :id")
            .setParameter("id", id).executeUpdate();
    }
}
