package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.CitizenFeedback;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "citizenFeedbackDAO")
@Transactional
public class CitizenFeedbackDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public CitizenFeedback save(CitizenFeedback fb) {
        entityManager.persist(fb);
        return fb;
    }

    public List<CitizenFeedback> getAll() {
        return entityManager
            .createQuery("from CitizenFeedback order by id desc", CitizenFeedback.class)
            .getResultList();
    }

    public CitizenFeedback getById(int id) {
        return entityManager.find(CitizenFeedback.class, id);
    }

    public List<CitizenFeedback> getByStatus(String status) {
        return entityManager
            .createQuery("from CitizenFeedback where status = :s order by id desc",
                         CitizenFeedback.class)
            .setParameter("s", status)
            .getResultList();
    }

    public List<CitizenFeedback> getByCategory(String category) {
        return entityManager
            .createQuery("from CitizenFeedback where category = :c order by id desc",
                         CitizenFeedback.class)
            .setParameter("c", category)
            .getResultList();
    }

    public CitizenFeedback update(CitizenFeedback fb) {
        return entityManager.merge(fb);
    }

    public void delete(int id) {
        CitizenFeedback fb = getById(id);
        if (fb != null) {
            entityManager.remove(fb);
        }
    }

    public long countByStatus(String status) {
        return (long) entityManager
            .createQuery("select count(f) from CitizenFeedback f where f.status = :s")
            .setParameter("s", status)
            .getSingleResult();
    }

    public Double avgRating() {
        Object result = entityManager
            .createQuery("select avg(f.rating) from CitizenFeedback f where f.rating is not null")
            .getSingleResult();
        return result == null ? 0.0 : (Double) result;
    }
}
