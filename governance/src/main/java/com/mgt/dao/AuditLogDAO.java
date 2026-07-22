package com.mgt.dao;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.mgt.model.AuditLog;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;

@Repository(value = "auditLogDAO")
@Transactional
public class AuditLogDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public AuditLog save(AuditLog log) {
        entityManager.persist(log);
        return log;
    }

    public List<AuditLog> getAll() {
        return entityManager
            .createQuery("from AuditLog order by id desc", AuditLog.class)
            .getResultList();
    }

    public List<AuditLog> getByUsername(String username) {
        return entityManager
            .createQuery("from AuditLog where username = :u order by id desc", AuditLog.class)
            .setParameter("u", username)
            .getResultList();
    }

    public List<AuditLog> getByModule(String module) {
        return entityManager
            .createQuery("from AuditLog where module = :m order by id desc", AuditLog.class)
            .setParameter("m", module)
            .getResultList();
    }
    
    public List<AuditLog> findAllByOrderByCreatedAtDesc() {
        return entityManager
            .createQuery("from AuditLog order by createdAt desc", AuditLog.class)
            .getResultList();
    }

    public AuditLog getById(int id) {
        return entityManager.find(AuditLog.class, id);
    }

    public void deleteAll() {
        entityManager.createQuery("delete from AuditLog").executeUpdate();
    }
}
