package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.ProjectList;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "projectListDAO")
@Transactional
public class ProjectListDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	public void save(ProjectList project) {
		entityManager.persist(project);
	}
	
	public List<ProjectList> getall() {
		String sql = "from ProjectList";
		List<ProjectList> projects = entityManager.createQuery(sql).getResultList();
		return projects;
	}
	
	public ProjectList getById(int id) {
        return entityManager.find(ProjectList.class, id);
    }

	public void updateStatus(int id, String status) {
		ProjectList project = entityManager.find(ProjectList.class, id);
        if (project != null) {
        	project.setStatus(status);
            entityManager.merge(project);
        }
	}
	
	public void updateProgress(int id, int progress) {
	    ProjectList p = entityManager.find(ProjectList.class, id);
	    if (p != null) {p.setProgress(progress);
	    }
	}
	
	public void update(ProjectList project) {
		entityManager.merge(project);
	}
	
	public void delete(int id) {
		entityManager.createQuery("delete from ProjectList where id = :id")
		.setParameter("id", id)
		.executeUpdate();
	}
}
