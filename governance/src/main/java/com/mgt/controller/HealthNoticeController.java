package com.mgt.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgt.model.HealthNotice;
import com.mgt.service.HealthNoticeService;


@RestController
@RequestMapping(value = "/api/health-notice")
public class HealthNoticeController {

	@Autowired
	private HealthNoticeService healthService;
	
	@PostMapping("/create")
	public void create (@RequestBody HealthNotice health) {
		healthService.create(health);
	}
	
	@GetMapping("/getall")
	public List<HealthNotice> getall() {
		return healthService.getall();
	}
	
	@PutMapping("/status/{id}")
    public void updateStatus(@PathVariable int id, @RequestBody String status) {
		healthService.updateStatus(id, status);
    }
	
	@PutMapping("/update/{id}")
	public void update(@PathVariable int id, @RequestBody HealthNotice health) {
		healthService.update(health);
	}
	
	@DeleteMapping("/{id}")
	public void delete(@PathVariable int id) {
		healthService.delete(id);
	}
}
