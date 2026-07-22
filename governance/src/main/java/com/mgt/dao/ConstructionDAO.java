package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.Construction;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "constructionDAO")
@Transactional
public class ConstructionDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	public void save(Construction construction) {
		entityManager.persist(construction);
	}
	
	public List<Construction> getall() {
		String sql = "from Construction";
		List<Construction> constructions = entityManager.createQuery(sql).getResultList();
		return constructions;
	}

	public List<Construction> findByContact(String contact) {
		return entityManager.createQuery("from Construction where contact = :contact", Construction.class)
				.setParameter("contact", contact).getResultList();
	}

	public Construction getById(int id) {
		return entityManager.find(Construction.class, id);
	}

	public void updateStatus(int id, String status) {
		Construction construction = entityManager.find(Construction.class, id);
        if (construction != null) {
        	construction.setStatus(status);
            entityManager.merge(construction);
        }
	}

	public void updateLocation(int id, Double lat, Double lng) {
		Construction construction = entityManager.find(Construction.class, id);
		if (construction != null) {
			construction.setLat(lat);
			construction.setLng(lng);
			entityManager.merge(construction);
		}
	}
}