package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.ETenderNotice;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "eTenderNoticeDAO")
@Transactional
public class ETenderNoticeDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public ETenderNotice save(ETenderNotice notice) {
        entityManager.persist(notice);
        return notice;
    }

    public List<ETenderNotice> getAll() {
        return entityManager
            .createQuery("FROM ETenderNotice ORDER BY id DESC", ETenderNotice.class)
            .getResultList();
    }

    public List<ETenderNotice> getByStatus(String status) {
        return entityManager
            .createQuery("FROM ETenderNotice WHERE status = :s ORDER BY id DESC", ETenderNotice.class)
            .setParameter("s", status)
            .getResultList();
    }

    public ETenderNotice getById(int id) {
        return entityManager.find(ETenderNotice.class, id);
    }

    public void updateStatus(int id, String status) {
        ETenderNotice notice = entityManager.find(ETenderNotice.class, id);
        if (notice != null) {
            notice.setStatus(status);
            entityManager.merge(notice);
        }
    }

    public ETenderNotice update(ETenderNotice notice) {
        return entityManager.merge(notice);
    }

    public void delete(int id) {
        ETenderNotice notice = entityManager.find(ETenderNotice.class, id);
        if (notice != null) entityManager.remove(notice);
    }
}
