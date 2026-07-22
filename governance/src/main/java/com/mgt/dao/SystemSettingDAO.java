package com.mgt.dao;

import java.util.List;
import org.springframework.stereotype.Repository;
import com.mgt.model.SystemSetting;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;

@Repository(value = "systemSettingDAO")
@Transactional
public class SystemSettingDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public SystemSetting save(SystemSetting s) {
        entityManager.persist(s);
        return s;
    }

    public List<SystemSetting> getAll() {
        return entityManager
            .createQuery("from SystemSetting order by id asc", SystemSetting.class)
            .getResultList();
    }

    public List<SystemSetting> getByCategory(String category) {
        return entityManager
            .createQuery("from SystemSetting where category = :c order by id asc",
                         SystemSetting.class)
            .setParameter("c", category)
            .getResultList();
    }

    public SystemSetting getById(int id) {
        return entityManager.find(SystemSetting.class, id);
    }

    public SystemSetting getByKey(String key) {
        try {
            return entityManager
                .createQuery("from SystemSetting where settingKey = :k", SystemSetting.class)
                .setParameter("k", key)
                .getSingleResult();
        } catch (NoResultException e) { return null; }
    }

    public SystemSetting update(SystemSetting s) {
        return entityManager.merge(s);
    }
}
