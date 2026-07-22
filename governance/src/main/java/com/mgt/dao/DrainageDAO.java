package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.Drainage;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "drainageDAO")
@Transactional
public class DrainageDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	public void save(Drainage drainage) {
		entityManager.persist(drainage);
	}
	
	public List<Drainage> getall() {
		String sql = "from Drainage";
		List<Drainage> drainages = entityManager.createQuery(sql).getResultList();
		return drainages;
	}

	public List<Drainage> findByContact(String contact) {
		return entityManager.createQuery("from Drainage where contact = :contact", Drainage.class)
				.setParameter("contact", contact).getResultList();
	}
	
	public Drainage getById(int id) {
        return entityManager.find(Drainage.class, id);
    }

	public void updateStatus(int id, String status) {
		Drainage drainage = entityManager.find(Drainage.class, id);
        if (drainage != null) {
        	drainage.setStatus(status);
            entityManager.merge(drainage);
        }
		
	}

	public void updateLocation(int id, Double lat, Double lng) {
		Drainage drainage = entityManager.find(Drainage.class, id);
		if (drainage != null) {
			drainage.setLat(lat);
			drainage.setLng(lng);
			entityManager.merge(drainage);
		}
	}
}