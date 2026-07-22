package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mgt.dao.ProjectBudgetDAO;
import com.mgt.model.ProjectBudget;

@Service
public class ProjectBudgetService {

	@Autowired
	ProjectBudgetDAO budgetDAO;
	
	public void create(ProjectBudget budget) {
		budgetDAO.save(budget);
	}
	
	public List<ProjectBudget> getall() {
		return budgetDAO.getall();
	}
	
	public ProjectBudget getById(int id) {
        return budgetDAO.getById(id);
    }
	
	public void updateStatus(int id, String status) {
		budgetDAO.updateStatus(id, status);
    }
	
	public void update(ProjectBudget budget) {
		budgetDAO.update(budget);
	}
	
	public void delete(int id) {
		budgetDAO.delete(id);
	}
}
