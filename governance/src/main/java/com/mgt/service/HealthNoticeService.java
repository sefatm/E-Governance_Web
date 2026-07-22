package com.mgt.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.mgt.dao.HealthNoticeDAO;
import com.mgt.model.HealthNotice;

@Service
public class HealthNoticeService {

	@Autowired
	HealthNoticeDAO noticeDAO;
	
	public void create(HealthNotice health) {
		noticeDAO.save(health);
	}
	
	public List<HealthNotice> getall() {
		return noticeDAO.getall();
	}
	
	public void updateStatus(int id, String status) {
		noticeDAO.updateStatus(id, status);
    }

	public void update(HealthNotice health) {
		noticeDAO.update(health);
	}
	
	public void delete(int id) {
		noticeDAO.delete(id);
	}
}
