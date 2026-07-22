package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.WaterConnection;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "waterConnectionDAO")
@Transactional
public class WaterConnectionDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	public void save(WaterConnection water) {
		entityManager.persist(water);
	}
	
	public List<WaterConnection> getall() {
		String sql = "from WaterConnection";
		List<WaterConnection> waters = entityManager.createQuery(sql).getResultList();
		return waters;
	}
	
	public WaterConnection getById(int id) {
        return entityManager.find(WaterConnection.class, id);
    }

	public void updateStatus(int id, String status) {
		WaterConnection water = entityManager.find(WaterConnection.class, id);
        if (water != null) {
        	water.setStatus(status);
            entityManager.merge(water);
        }
	}
	
	public void update(WaterConnection water) {
		entityManager.merge(water);
	}
	
	public void delete(int id) {
		entityManager.createQuery("delete from WaterConnection where id = :id")
		.setParameter("id", id)
		.executeUpdate();
	}
}
