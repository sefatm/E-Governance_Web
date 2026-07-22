package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.PickupRequest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "pickupRequestDAO")
@Transactional
public class PickupRequestDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	public void save(PickupRequest waste) {
		entityManager.persist(waste);
	}
	
	public List<PickupRequest> getall() {
		String sql = "from PickupRequest";
		List<PickupRequest> wastes = entityManager.createQuery(sql).getResultList();
		return wastes;
	}
	
	public List<PickupRequest> findByPhone(String phone) {
        return entityManager.createQuery("from PickupRequest where phone = :phone order by createdAt desc", PickupRequest.class)
            .setParameter("phone", phone).getResultList();
    }

	public PickupRequest getById(int id) {
        return entityManager.find(PickupRequest.class, id);
    }

	public void updateStatus(int id, String status) {
		PickupRequest waste = entityManager.find(PickupRequest.class, id);
        if (waste != null) {
        	waste.setStatus(status);
            entityManager.merge(waste);
        }
	}
	
	public void update(PickupRequest waste) {
		entityManager.merge(waste);
	}
	
	public void delete(int id) {
		entityManager.createQuery("delete from PickupRequest where id = :id")
		.setParameter("id", id)
		.executeUpdate();
	}
}
