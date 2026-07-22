package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.Road;
import com.mgt.service.RoadService;

@RestController
@RequestMapping(value = "/api/road")
public class RoadController {

    @Autowired 
    RoadService roadService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody Road road) {
        try {
            roadService.create(road);
            return ResponseEntity.ok(Map.of("message", "Application submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public List<Road> getall() { return roadService.getall(); }

    // Citizen endpoint — returns only applications matching caller's contact number
    @GetMapping("/my-applications")
    public List<Road> myApplications(@RequestParam String contact) {
        return roadService.findByContact(contact);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        Road r = roadService.getById(id);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(r);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        roadService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @PutMapping("/location/{id}")
    public ResponseEntity<Object> updateLocation(@PathVariable int id, @RequestBody Map<String, Double> body) {
        Double lat = body.containsKey("lat") ? body.get("lat") : body.get("latitude");
        Double lng = body.containsKey("lng") ? body.get("lng") : body.get("longitude");
        roadService.updateLocation(id, lat, lng);
        return ResponseEntity.ok(Map.of("message", "Location updated"));
    }
}
