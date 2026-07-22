package com.mgt.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.mgt.model.ProjectBudget;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@Repository(value = "projectBudgetDAO")
@Transactional
public class ProjectBudgetDAO {

	@PersistenceContext
	private EntityManager entityManager;
	
	public void save(ProjectBudget budget) {
		entityManager.persist(budget);
	}
	
	public List<ProjectBudget> getall() {
		String sql = "from ProjectBudget";
		List<ProjectBudget> budgets = entityManager.createQuery(sql).getResultList();
		return budgets;
	}
	
	public ProjectBudget getById(int id) {
        return entityManager.find(ProjectBudget.class, id);
    }

	public void updateStatus(int id, String status) {
		ProjectBudget budget = entityManager.find(ProjectBudget.class, id);
        if (budget != null) {
        	budget.setStatus(status);
            entityManager.merge(budget);
        }
	}
	
	public void update(ProjectBudget project) {
		entityManager.merge(project);
	}
	
	public void delete(int id) {
		entityManager.createQuery("delete from ProjectBudget where id = :id")
		.setParameter("id", id)
		.executeUpdate();
	}
}
