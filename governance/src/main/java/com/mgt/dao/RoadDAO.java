package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;
import com.mgt.model.Road;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "roadDAO")
@Transactional
public class RoadDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	public void save(Road road) {
		entityManager.persist(road);
	}
	
	public List<Road> getall() {
		String sql = "from road";
		List<Road> roads = entityManager.createQuery(sql).getResultList();
		return roads;
	}

	public List<Road> findByContact(String contact) {
		return entityManager.createQuery("from road where contact = :contact", Road.class)
				.setParameter("contact", contact).getResultList();
	}
	
	public Road getById(int id) {
        return entityManager.find(Road.class, id);
    }

	public void updateStatus(int id, String status) {
		Road road = entityManager.find(Road.class, id);
        if (road != null) {
            road.setStatus(status);
            entityManager.merge(road);
        }
	}
	
	public void updateLocation(int id, Double lat, Double lng) {
        var item = entityManager.find(Road.class, id);
        if (item != null) {
            item.setLat(lat);
            item.setLng(lng);
            entityManager.merge(item);
        }
    }

}
