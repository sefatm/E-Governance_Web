package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.NotificationMessage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "notificationMessageDAO")
@Transactional
public class NotificationMessageDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public NotificationMessage save(NotificationMessage msg) {
        entityManager.persist(msg);
        return msg;
    }

    public List<NotificationMessage> getAll() {
        return entityManager
            .createQuery("from NotificationMessage order by id desc",
                         NotificationMessage.class)
            .getResultList();
    }

    public NotificationMessage getById(int id) {
        return entityManager.find(NotificationMessage.class, id);
    }

    public List<NotificationMessage> getByType(String type) {
        return entityManager
            .createQuery("from NotificationMessage where type = :t order by id desc",
                         NotificationMessage.class)
            .setParameter("t", type)
            .getResultList();
    }

    public List<NotificationMessage> getByServiceTag(String tag) {
        return entityManager
            .createQuery("from NotificationMessage where serviceTag = :tag order by id desc",
                         NotificationMessage.class)
            .setParameter("tag", tag)
            .getResultList();
    }

    public void delete(int id) {
        NotificationMessage msg = entityManager.find(NotificationMessage.class, id);
        if (msg != null) entityManager.remove(msg);
    }

    public long countByType(String type) {
        return (long) entityManager
            .createQuery("select count(n) from NotificationMessage n where n.type = :t")
            .setParameter("t", type)
            .getSingleResult();
    }
}
