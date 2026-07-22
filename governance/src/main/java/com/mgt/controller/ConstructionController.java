package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.Construction;
import com.mgt.service.ConstructionService;


@RestController
@RequestMapping(value = "/api/construction")
public class ConstructionController {

    @Autowired ConstructionService constructionService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody Construction construction) {
        try {
            constructionService.create(construction);
            return ResponseEntity.ok(Map.of("message", "Application submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public List<Construction> getall() {
        return constructionService.getall();
    }

    @GetMapping("/my-applications")
    public List<Construction> myApplications(@RequestParam String contact) {
        return constructionService.findByContact(contact);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        constructionService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @PutMapping("/location/{id}")
    public ResponseEntity<Object> updateLocation(@PathVariable int id, @RequestBody java.util.Map<String, Double> body) {
        try {
            constructionService.updateLocation(id, body.get("lat"), body.get("lng"));
            return ResponseEntity.ok(java.util.Map.of("message", "Location updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}