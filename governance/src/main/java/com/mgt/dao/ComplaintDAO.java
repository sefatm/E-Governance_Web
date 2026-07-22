package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.Complaint;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;


@Repository(value = "complaintDAO")
@Transactional
public class ComplaintDAO {

    @PersistenceContext
    private EntityManager entityManager;

    public void save(Complaint complaint) {
        entityManager.persist(complaint);
    }

    public List<Complaint> getall() {
        return entityManager.createQuery("from complaint", Complaint.class).getResultList();
    }

    public List<Complaint> findByContact(String contact) {
        return entityManager
            .createQuery("from complaint c where c.contact = :contact", Complaint.class)
            .setParameter("contact", contact)
            .getResultList();
    }

    public void updateStatus(int id, String status) {
        Complaint c = entityManager.find(Complaint.class, id);
        if (c != null) { c.setStatus(status); entityManager.merge(c); }
    }

    public Complaint findById(int id) {
        return entityManager.find(Complaint.class, id);
    }

    public void updateLocation(int id, Double lat, Double lng) {
        Complaint c = entityManager.find(Complaint.class, id);
        if (c != null) {
            c.setLat(lat);
            c.setLng(lng);
            entityManager.merge(c);
        }
    }

    public void updateRemarks(int id, String remarks) {
        Complaint c = entityManager.find(Complaint.class, id);
        if (c != null) { c.setRemarks(remarks); entityManager.merge(c); }
    }
}
