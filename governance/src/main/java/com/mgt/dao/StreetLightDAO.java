package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.StreetLight;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "lightDAO")
@Transactional
public class StreetLightDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	public void save(StreetLight light) {
		entityManager.persist(light);
	}
	
	public List<StreetLight> getall() {
		String sql = "from StreetLight";
		List<StreetLight> lights = entityManager.createQuery(sql).getResultList();
		return lights;
	}

	public List<StreetLight> findByContact(String contact) {
		return entityManager.createQuery("from StreetLight where contact = :contact", StreetLight.class)
				.setParameter("contact", contact).getResultList();
	}

	public void updateStatus(int id, String status) {
		StreetLight light = entityManager.find(StreetLight.class, id);
        if (light != null) {
        	light.setStatus(status);
            entityManager.merge(light);
        }
		
	}

	public void updateLocation(int id, Double lat, Double lng) {
		StreetLight light = entityManager.find(StreetLight.class, id);
		if (light != null) {
			light.setLat(lat);
			light.setLng(lng);
			entityManager.merge(light);
		}
	}
}