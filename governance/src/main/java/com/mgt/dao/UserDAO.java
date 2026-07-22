package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.AppUser;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository
@Transactional
public class UserDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(AppUser user) {
        entityManager.persist(user);
    }

    public List<AppUser> getAll() {
        return entityManager
            .createQuery("from appUser order by createdAt desc", AppUser.class)
            .getResultList();
    }

    public List<AppUser> getPending() {
        return entityManager
            .createQuery("from appUser u where u.status = 'Pending' order by u.createdAt desc", AppUser.class)
            .getResultList();
    }

    public AppUser findByEmail(String email) {
        List<AppUser> list = entityManager
            .createQuery("from appUser u where u.email = :email", AppUser.class)
            .setParameter("email", email)
            .getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    public AppUser getById(int id) {
        entityManager.clear();
        return entityManager.find(AppUser.class, id);
    }

    public void updateRole(int id, String role) {
        AppUser u = entityManager.find(AppUser.class, id);
        if (u != null) { u.setRole(role); entityManager.merge(u); }
    }

    public void updateStatus(int id, String status) {
        AppUser u = entityManager.find(AppUser.class, id);
        if (u != null) { u.setStatus(status); entityManager.merge(u); }
    }

    public void approve(int id, String role) {
        AppUser u = entityManager.find(AppUser.class, id);
        if (u != null) {
            u.setStatus("Active");
            if (role != null && !role.isBlank()) u.setRole(role);
            entityManager.merge(u);
        }
    }

    public void delete(int id) {
        entityManager
            .createQuery("delete from appUser u where u.id = :id")
            .setParameter("id", id)
            .executeUpdate();
    }

    public void changePassword(int id, String hashedPassword) {
        AppUser u = entityManager.find(AppUser.class, id);
        if (u != null) {
            u.setPassword(hashedPassword);
            entityManager.merge(u);
        }
    }

    public void updatePhoto(int id, String photoUrl) {
        entityManager.createQuery(
            "UPDATE appUser u SET u.photoUrl = :photoUrl WHERE u.id = :id")
            .setParameter("photoUrl", photoUrl)
            .setParameter("id", id)
            .executeUpdate();
        entityManager.flush();   
        entityManager.clear();  
    }

    public void updateProfile(int id, String name, String email) {
        entityManager.createQuery(
            "UPDATE appUser u SET u.name = :name, u.email = :email WHERE u.id = :id")
            .setParameter("name", name)
            .setParameter("email", email)
            .setParameter("id", id)
            .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }
}
