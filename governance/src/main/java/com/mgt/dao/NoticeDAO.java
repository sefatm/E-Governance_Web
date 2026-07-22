package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.Notice;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "NoticeDAO")
@Transactional
public class NoticeDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(Notice notice) {
        entityManager.persist(notice);
    }

    public List<Notice> getAll() {
        return entityManager
            .createQuery("from Notice order by createdAt desc", Notice.class)
            .getResultList();
    }

    public List<Notice> getByType(String type) {
        return entityManager
            .createQuery("from Notice n where n.type = :type order by createdAt desc", Notice.class)
            .setParameter("type", type)
            .getResultList();
    }

    public List<Notice> getActive() {
        return entityManager
            .createQuery(
                "from Notice n where n.status = 'Active' and (n.expiryDate is null or n.expiryDate >= CURRENT_DATE) order by n.createdAt desc",
                Notice.class
            )
            .getResultList();
    }

    public Notice getById(int id) {
        return entityManager.find(Notice.class, id);
    }

    public void update(Notice notice) {
        entityManager.merge(notice);
    }

    public void updateStatus(int id, String status) {
        Notice notice = entityManager.find(Notice.class, id);
        if (notice != null) {
            notice.setStatus(status);
            entityManager.merge(notice);
        }
    }

    public void delete(int id) {
        entityManager
            .createQuery("delete from Notice n where n.id = :id")
            .setParameter("id", id)
            .executeUpdate();
    }
}
