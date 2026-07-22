package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mgt.dao.HealthCenterDAO;
import com.mgt.model.HealthCenter;

@Service
public class HealthCenterService {
	
	@Autowired
	HealthCenterDAO healthDAO;
	
	public void create(HealthCenter health) {
		healthDAO.save(health);
	}
	
	public List<HealthCenter> getall() {
		return healthDAO.getall();
	}
	
	public HealthCenter getById(int id) {
        return healthDAO.getById(id);
    }
	
	public void updateStatus(int id, String status) {
		healthDAO.updateStatus(id, status);
    }
	
    public void updateLocation(int id, Double lat, Double lng) {
        healthDAO.updateLocation(id, lat, lng);
    }

	public void delete(int id) {
		healthDAO.delete(id);
	}
}
