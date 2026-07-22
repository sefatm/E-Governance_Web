package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.GarbageSchedule;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "garbageScheduleDAO")
@Transactional
public class GarbageScheduleDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	public void save(GarbageSchedule garbage) {
		entityManager.persist(garbage);
	}
	
	public List<GarbageSchedule> getall() {
		String sql = "from GarbageSchedule";
		List<GarbageSchedule> garbages = entityManager.createQuery(sql).getResultList();
		return garbages;
	}
	
	public GarbageSchedule getById(int id) {
        return entityManager.find(GarbageSchedule.class, id);
    }

	public void updateStatus(int id, String status) {
		GarbageSchedule garbage = entityManager.find(GarbageSchedule.class, id);
        if (garbage != null) {
        	garbage.setStatus(status);
            entityManager.merge(garbage);
        }
	}
	
	public void update(GarbageSchedule garbage) {
		entityManager.merge(garbage);
	}
	
	public void delete(int id) {
		entityManager.createQuery("delete from GarbageSchedule where id = :id")
		.setParameter("id", id)
		.executeUpdate();
	}
}
