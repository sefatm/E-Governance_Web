package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.Sanitation;
// FIX: import com.mgt.model.Road — সরানো হয়েছে (unused import, compile warning)

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "sanitationDAO")
@Transactional
public class SanitationDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(Sanitation sanitation) {
        entityManager.persist(sanitation);
    }

    public List<Sanitation> getall() {
        return entityManager.createQuery("from Sanitation", Sanitation.class).getResultList();
    }

    public Sanitation getById(int id) {
        return entityManager.find(Sanitation.class, id);
    }

    public void updateStatus(int id, String status) {
        Sanitation sanitation = entityManager.find(Sanitation.class, id);
        if (sanitation != null) {
            sanitation.setStatus(status);
            entityManager.merge(sanitation);
        }
    }

    public void delete(int id) {
        entityManager.createQuery("delete from Sanitation where id = :id")
            .setParameter("id", id)
            .executeUpdate();
    }
}
