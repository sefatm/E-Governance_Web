package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.HealthNotice;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "HealthNoticeDAO")
@Transactional
public class HealthNoticeDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	public void save(HealthNotice notice) {
		entityManager.persist(notice);
	}
	
	public List<HealthNotice> getall() {
		String sql = "from HealthNotice";
		List<HealthNotice> notices = entityManager.createQuery(sql).getResultList();
		return notices;
	}

	public void updateStatus(int id, String status) {
		HealthNotice notice = entityManager.find(HealthNotice.class, id);
        if (notice != null) {
        	notice.setStatus(status);
            entityManager.merge(notice);
        }
		
	}

	public void update(HealthNotice notice) {
		entityManager.merge(notice);
	}
	
	public void delete(int id) {
		entityManager.createQuery("delete from HealthNotice h where h.id = :id")
		.setParameter("id", id)
		.executeUpdate();
	}

}
