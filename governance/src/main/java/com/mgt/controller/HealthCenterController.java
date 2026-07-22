package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgt.model.HealthCenter;
import com.mgt.service.HealthCenterService;

@RestController
@RequestMapping(value = "/api/health-center")
public class HealthCenterController {

	@Autowired
	HealthCenterService healthService;
	
	@PostMapping("/create")
	public void create (@RequestBody HealthCenter health) {
		healthService.create(health);
	}
	
	@GetMapping("/getall")
	public List<HealthCenter> getall() {
		return healthService.getall();
	}
	
	@GetMapping("/{id}")
    public HealthCenter getById(@PathVariable int id) {
        return healthService.getById(id);
    }
	
	@PutMapping("/status/{id}")
    public void updateStatus(@PathVariable int id, @RequestBody String status) {
		healthService.updateStatus(id, status);
    }
	
    @PutMapping("/location/{id}")
    public ResponseEntity<Object> updateLocation(@PathVariable int id, @RequestBody Map<String, Double> body) {
        Double lat = body.containsKey("lat") ? body.get("lat") : body.get("latitude");
        Double lng = body.containsKey("lng") ? body.get("lng") : body.get("longitude");
        if (lat == null || lng == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "lat and lng are required"));
        }
        healthService.updateLocation(id, lat, lng);
        return ResponseEntity.ok(Map.of("message", "Health center location updated"));
    }

	@DeleteMapping("/delete/{id}")
	public void delete(@PathVariable int id) {
		healthService.delete(id);
	}
}
