package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.ETenderAward;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "eTenderAwardDAO")
@Transactional
public class ETenderAwardDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public ETenderAward save(ETenderAward award) {
        entityManager.persist(award);
        return award;
    }

    public List<ETenderAward> getAll() {
        return entityManager
            .createQuery("FROM ETenderAward ORDER BY id DESC", ETenderAward.class)
            .getResultList();
    }

    public ETenderAward getById(int id) {
        return entityManager.find(ETenderAward.class, id);
    }

    public ETenderAward getByTenderId(int tenderId) {
        try {
            return entityManager
                .createQuery("FROM ETenderAward WHERE tenderId = :tid", ETenderAward.class)
                .setParameter("tid", tenderId)
                .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
}
