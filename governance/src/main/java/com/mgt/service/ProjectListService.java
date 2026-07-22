package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.mgt.dao.ProjectListDAO;
import com.mgt.model.ProjectList;

@Service
public class ProjectListService {

	@Autowired
	ProjectListDAO projectDAO;
	
	public void create(ProjectList project) {
		projectDAO.save(project);
	}
	
	public List<ProjectList> getall() {
		return projectDAO.getall();
	}
	
	public ProjectList getById(int id) {
        return projectDAO.getById(id);
    }
	
	public void updateStatus(int id, String status) {
		projectDAO.updateStatus(id, status);
    }
	
	public void update(ProjectList project) {
		projectDAO.update(project);
	}
	
	public void delete(int id) {
		projectDAO.delete(id);
	}

	public void updateProgress(int id, int progress) {
	    projectDAO.updateProgress(id, progress);
	}
	
}
