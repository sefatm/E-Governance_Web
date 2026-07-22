package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.HealthCenter;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "healthDAO")
@Transactional
public class HealthCenterDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	public void save(HealthCenter health) {
		entityManager.persist(health);
	}
	
	public List<HealthCenter> getall() {
		String sql = "from HealthCenter";
		List<HealthCenter> healths = entityManager.createQuery(sql).getResultList();
		return healths;
	}
	
	public HealthCenter getById(int id) {
        return entityManager.find(HealthCenter.class, id);
    }

	public void updateStatus(int id, String status) {
		HealthCenter health = entityManager.find(HealthCenter.class, id);
        if (health != null) {
        	health.setStatus(status);
            entityManager.merge(health);
        }	
	}
	
    public void updateLocation(int id, Double lat, Double lng) {
        HealthCenter health = entityManager.find(HealthCenter.class, id);
        if (health != null) {
            health.setLat(lat);
            health.setLng(lng);
            entityManager.merge(health);
        }
    }

	public void delete(int id) {
		entityManager.createQuery("delete from HealthCenter where id = :id")
		.setParameter("id", id)
		.executeUpdate();
	}
}
