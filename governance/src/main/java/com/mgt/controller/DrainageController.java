package com.mgt.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mgt.model.Drainage;
import com.mgt.service.DrainageService;


@RestController
@RequestMapping(value = "/api/drainage")
public class DrainageController {

    @Autowired DrainageService drainageService;

    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody Drainage drainage) {
        try {
            drainageService.create(drainage);
            return ResponseEntity.ok(Map.of("message", "Application submitted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/getall")
    public List<Drainage> getall() {
        return drainageService.getall();
    }

    @GetMapping("/my-applications")
    public List<Drainage> myApplications(@RequestParam String contact) {
        return drainageService.findByContact(contact);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getById(@PathVariable int id) {
        Drainage d = drainageService.getById(id);
        if (d == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(d);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<Object> updateStatus(@PathVariable int id, @RequestBody Map<String, String> body) {
        drainageService.updateStatus(id, body.get("status"));
        return ResponseEntity.ok(Map.of("message", "Status updated"));
    }

    @PutMapping("/location/{id}")
    public ResponseEntity<Object> updateLocation(@PathVariable int id, @RequestBody java.util.Map<String, Double> body) {
        try {
            drainageService.updateLocation(id, body.get("lat"), body.get("lng"));
            return ResponseEntity.ok(java.util.Map.of("message", "Location updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}